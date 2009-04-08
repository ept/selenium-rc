/*
 * Created on Feb 26, 2006
 *
 */
package org.openqa.selenium.server.htmlrunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.FileSet;
import org.mortbay.log.LogFactory;
import org.openqa.selenium.server.BrowserConfigurationOptions;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumCommandTimedOutException;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.StaticContentHandler;
import org.openqa.selenium.server.BrowserSessionFactory.BrowserSessionInfo;
import org.openqa.selenium.server.browserlaunchers.AsyncExecute;
import org.openqa.selenium.server.browserlaunchers.BrowserLauncher;
import org.openqa.selenium.server.browserlaunchers.BrowserLauncherFactory;
import org.openqa.selenium.server.browserlaunchers.LauncherUtils;
import org.openqa.selenium.server.browserlaunchers.WindowsUtils;

/**
 * Runs HTML Selenium test suites.
 *  
 * 
 *  @author dfabulich
 *
 */
public class HTMLLauncher implements HTMLResultsListener {

    static Log log = LogFactory.getLog(HTMLLauncher.class);
    private SeleniumServer remoteControl;
    private HTMLTestResults results;

	public HTMLLauncher(SeleniumServer remoteControl) {
        this.remoteControl = remoteControl;
    }
    
    /** Launches a single HTML Selenium test suite.
     * 
     * @param browser - the browserString ("*firefox", "*iexplore" or an executable path)
     * @param browserURL - the start URL for the browser
     * @param suiteURL - the relative URL to the HTML suite
     * @param outputFile - The file to which we'll output the HTML results
     * @param timeoutInSeconds - the amount of time (in seconds) to wait for the browser to finish
     * @param multiWindow TODO
     * @return PASS or FAIL
     * @throws IOException if we can't write the output file
     */
    public String runHTMLSuite(String browser, String browserURL, String suiteURL, File outputFile, int timeoutInSeconds, boolean multiWindow) throws IOException {
        return runHTMLSuite(browser, browserURL, suiteURL, outputFile,
                timeoutInSeconds, multiWindow, "info");
    }
    
    protected BrowserLauncher getBrowserLauncher(String browser, String sessionId, RemoteControlConfiguration configuration, BrowserConfigurationOptions browserOptions) {
    	BrowserLauncherFactory blf = new BrowserLauncherFactory();
    	return blf.getBrowserLauncher(browser, sessionId, configuration, browserOptions);
    }
    
    protected void sleepTight(long timeoutInMs) {
        long now = System.currentTimeMillis();
        long end = now + timeoutInMs;
        while (results == null && System.currentTimeMillis() < end) {
            AsyncExecute.sleepTight(500);
        }
    }
    
    protected FileWriter getFileWriter(File outputFile) throws IOException {
    	return new FileWriter(outputFile);
    }
    
    protected void writeResults(File outputFile) throws IOException {
        if (outputFile != null) {
            FileWriter fw = getFileWriter(outputFile);
            results.write(fw);
            fw.close();
        }
    }

    /** Launches a single HTML Selenium test suite.
     * 
     * @param browser - the browserString ("*firefox", "*iexplore" or an executable path)
     * @param browserURL - the start URL for the browser
     * @param suiteURL - the relative URL to the HTML suite
     * @param outputFile - The file to which we'll output the HTML results
     * @param multiWindow TODO
     * @param defaultLogLevel TODO
     * @param timeoutInSeconds - the amount of time (in seconds) to wait for the browser to finish
     * @return PASS or FAIL
     * @throws IOException if we can't write the output file
     */
    private String runHTMLSuite(String browser, String browserURL, String suiteURL, File outputFile, int timeoutInSeconds, boolean multiWindow, String defaultLogLevel) throws IOException {
        outputFile.createNewFile();
        if (!outputFile.canWrite()) {
        	throw new IOException("Can't write to outputFile: " + outputFile.getAbsolutePath());
        }
    	long timeoutInMs = 1000l * timeoutInSeconds;
        if (timeoutInMs < 0) {
            log.warn("Looks like the timeout overflowed, so resetting it to the maximum.");
            timeoutInMs = Long.MAX_VALUE;
        }
        
        RemoteControlConfiguration configuration = remoteControl.getConfiguration();
        remoteControl.handleHTMLRunnerResults(this);

        String sessionId = Long.toString(System.currentTimeMillis() % 1000000);
        BrowserConfigurationOptions browserOptions = new BrowserConfigurationOptions();
        
        configuration.copySettingsIntoBrowserOptions(browserOptions);
        
        browserOptions.setSingleWindow(!multiWindow);
        
        BrowserLauncher launcher = getBrowserLauncher(browser, sessionId, configuration, browserOptions);
        BrowserSessionInfo sessionInfo = new BrowserSessionInfo(sessionId, 
            browser, browserURL, launcher, null);
        
        remoteControl.registerBrowserSession(sessionInfo);
        
        // JB: -- aren't these URLs in the wrong order according to declaration?
        launcher.launchHTMLSuite(suiteURL, browserURL);
        
        sleepTight(timeoutInMs);
        
        launcher.close();
        
        remoteControl.deregisterBrowserSession(sessionInfo);
        
        if (results == null) {
            throw new SeleniumCommandTimedOutException();
        }
        
        writeResults(outputFile);
        
        return results.getResult().toUpperCase();
    }
    
    /** Launches a single HTML Selenium test suite.
     * 
     * @param browser - the browserString ("*firefox", "*iexplore" or an executable path)
     * @param browserURL - the start URL for the browser
     * @param suiteFile - a file containing the HTML suite to run
     * @param outputFile - The file to which we'll output the HTML results
     * @param timeoutInSeconds - the amount of time (in seconds) to wait for the browser to finish
     * @param multiWindow - whether to run the browser in multiWindow or else framed mode
     * @return PASSED or FAIL
     * @throws IOException if we can't write the output file
     */
    public String runHTMLSuite(String browser, String browserURL, File suiteFile, File outputFile, int timeoutInSeconds, boolean multiWindow) throws IOException {
        if (browser == null) throw new IllegalArgumentException("browser may not be null");
        if (!suiteFile.exists()) {
    		throw new IOException("Can't find HTML Suite file:" + suiteFile.getAbsolutePath());
    	}
    	if (!suiteFile.canRead()) {
    		throw new IOException("Can't read HTML Suite file: " + suiteFile.getAbsolutePath());
    	}
    	remoteControl.addNewStaticContent(suiteFile.getParentFile());
        
        // DGF this is a hack, but I can't find a better place to put it
        String suiteURL;
        if (browser.startsWith("*chrome") || browser.startsWith("*firefox") || browser.startsWith("*iehta")  || browser.startsWith("*iexplore") ) {
            suiteURL = "http://localhost:" + remoteControl.getConfiguration().getPortDriversShouldContact() + "/selenium-server/tests/" + suiteFile.getName();
        } else {
            suiteURL = LauncherUtils.stripStartURL(browserURL) + "/selenium-server/tests/" + suiteFile.getName();
        }
    	return runHTMLSuite(browser, browserURL, suiteURL, outputFile, timeoutInSeconds, multiWindow, "info");
    }
    
    
    /** Accepts HTMLTestResults for later asynchronous handling */
    public void processResults(HTMLTestResults resultsParm) {
        this.results = resultsParm;
    }

    public boolean runSelfTests(File dir) throws IOException {
        String[] browsers;
        if (WindowsUtils.thisIsWindows()) {
            browsers = new String[] { "firefox", "iexplore", "opera", "chrome",
                    }; // TODO safari // TODO "iehta" is just too unreliable!
        } else if (Os.isFamily("mac")) {
            browsers = new String[] {"firefox", "safari", "chrome"};
        } else { // assume Linux (a pretty bold assumption)
            browsers = new String[] {"firefox", "opera", "konqueror", "chrome"};
        }
        
        boolean allPassed = true;
        boolean result;
        for (String browser : browsers) {
            result = runSelfTest(dir, browser, true, false);
            if (!result) allPassed = false;
        }
        for (String browser : browsers) {
            result = runSelfTest(dir, browser, false, false);
            if (!result) allPassed = false;
        }
        for (String browser : browsers) {
            result = runSelfTest(dir, browser, true, true);
            if (!result) allPassed = false;
        }
//        DGF singleWindow slowResources never turns up anything
//        for (String browser : browsers) {
//            result = runSelfTest(dir, browser, true, false);
//            if (!result) allPassed = false;
//        }
        //runSelfTest(dir, browser, false, true); // DGF singleWindow slowResources never turns up anything
        if (allPassed) {
            log.info("ALL TESTS PASSED");
        } else {
            log.error("TESTS FAILED, see " + dir.getAbsolutePath());
        }
        bzipTestResults(dir);
        return allPassed;
    }

    private void bzipTestResults(File dir) {
        File destFile = new File(dir, "results.tar.bz2");
        Tar bzipTask = new Tar();
        Tar.TarCompressionMethod bzip2 = new Tar.TarCompressionMethod();
        bzip2.setValue("bzip2");
        bzipTask.setCompression(bzip2);
        bzipTask.setProject(new Project());
        bzipTask.setDestFile(destFile);
        FileSet fs = bzipTask.createTarFileSet();
        fs.setDir(dir);
        fs.setIncludes("*.html");
        bzipTask.execute();
        log.info("bzipped test results: " + destFile.getAbsolutePath());
    }

    private boolean runSelfTest(File dir, String browser, boolean multiWindow, boolean slowResources) throws IOException {
        String options = (multiWindow ? "multiWindow-" : "") + (slowResources ? "slowResources-" : "");
        String name = "results-" + browser + '-' + options + "TestSuite.html";
        File resultsFile = new File(dir, name);
        String baseUrl = "http://localhost:" + remoteControl.getPort();
        String suiteUrl = baseUrl + "/selenium-server/tests/TestSuite.html";
        StaticContentHandler.setSlowResources(slowResources);
        String result = null;
        int timeoutInSeconds = remoteControl.getConfiguration().getTimeoutInSeconds();
        try {
            result = runHTMLSuite("*"+browser, baseUrl, suiteUrl, resultsFile, timeoutInSeconds, multiWindow, "info");
            if ("PASSED".equals(result)) {
                log.info(result + ' ' + resultsFile.getAbsolutePath());
            } else {
                log.error(result + ' ' + resultsFile.getAbsolutePath());
            }
            
        } catch (SeleniumCommandTimedOutException e) {
            result = "FAIL (timed out)";
            log.error(result + ' ' + resultsFile.getAbsolutePath());
            FileWriter fw = new FileWriter(resultsFile);
            fw.write("<html><head><title>Error</title></head><body>Error: timed out after " + timeoutInSeconds + " seconds</body></html>");
            fw.close();
        } catch (Exception e) {
            result = "ERROR";
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.error(result + ' ' + resultsFile.getAbsolutePath(), e);
            FileWriter fw = new FileWriter(resultsFile);
            fw.write("<html><head><title>Error</title></head><body><pre>" +
                    HTMLTestResults.quoteCharacters(sw.toString()) +
            		"</pre></body></html>");
            fw.close();
        }
        results = null;
        return "PASSED".equals(result);
    }
    
    public static int mainInt(String... args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Please pass a directory argument on the command line");
        }
        File dir = new File(args[0]);
        dir.mkdirs();
        SeleniumServer server = new SeleniumServer();
        boolean result = false;
        try {
            server.start();
            result = new HTMLLauncher(server).runSelfTests(dir);
        } finally {
            server.stop();
        }
        return result ? 0 : 1;
    }
    
    public static void main(String[] args) throws Exception {
    	System.exit(mainInt(args));
    }
    
    public HTMLTestResults getResults() {
		return results;
	}

	public void setResults(HTMLTestResults results) {
		this.results = results;
	}
}
