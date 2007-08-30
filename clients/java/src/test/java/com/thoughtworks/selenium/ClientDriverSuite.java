/*
 * Created on Feb 25, 2006
 *
 */
package com.thoughtworks.selenium;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.corebased.*;

/**
 * The wrapper test suite for these tests, which spawns an in-process Selenium
 * Server for simple integration testing.
 * 
 * <p>
 * Normally, users should start the Selenium Server out-of-process, and just
 * leave it up and running, available for the tests to use. But, if you like,
 * you can do what we do here and start a Selenium Server before launching the
 * tests.
 * </p>
 * 
 * <p>
 * Note that we don't recommend starting and stopping the entire server during
 * each test's setUp and tearDown for these Integration tests; it shouldn't be
 * necessary, and doing so may conceal bugs in the server.
 * </p>
 * 
 * 
 * @author Dan Fabulich
 * 
 */
public class ClientDriverSuite extends TestCase {

    /**
     * Construct a test suite containing the other integration tests, wrapping
     * them up in a TestSetup object that will launch the Selenium Server
     * in-proc.
     * 
     * @return a test suite containing tests to run
     */
    public static Test suite() {
        boolean isProxyInjectionMode = System.getProperty("selenium.proxyInjectionMode") != null
        && System.getProperty("selenium.proxyInjectionMode").equals("true");

        String forcedBrowserMode = System.getProperty("selenium.forcedBrowserMode");
        
        TestSuite supersuite = new TestSuite(ClientDriverSuite.class.getName());
        TestSuite suite = generateSuite(isProxyInjectionMode, forcedBrowserMode);
        // Left here to be able to run non proxy injection mode tests in a PI mode server 
        //InitSystemPropertiesTestSetup setup = new ClientDriverPISuite.InitSystemPropertiesTestSetupForPImode(suite);
        
        // Decorate generated test suite with a decorator to initialize system properties
        // such as debugging and logging properties
        InitSystemPropertiesTestSetup setup = new InitSystemPropertiesTestSetup(suite);
        supersuite.addTest(setup);
        
        return supersuite;
    }

    public static TestSuite generateSuite(boolean isProxyInjectionMode, String forcedBrowserMode) {
        try {
            // TODO This class extends TestCase to workaround MSUREFIRE-113
            // http://jira.codehaus.org/browse/MSUREFIRE-113
            // Once that bug is fixed, this class should be a TestSuite, not a
            // TestCase
            TestSuite supersuite = new TestSuite(ClientDriverSuite.class
                    .getName());
            TestSuite suite = new TestSuite(ClientDriverSuite.class.getName());
            
            
            
            
               
            suite.addTestSuite(ApacheMyFacesSuggestTest.class);
            suite.addTest(I18nTest.suite());
            suite.addTestSuite(TestBasicAuth.class);
            suite.addTestSuite(RealDealIntegrationTest.class);
            suite.addTestSuite(TestErrorChecking.class);
            suite.addTestSuite(TestJavascriptParameters.class);
            suite.addTestSuite(TestClick.class);
            suite.addTestSuite(GoogleTestSearch.class);
            suite.addTestSuite(GoogleTest.class);
            suite.addTestSuite(WindowNamesTest.class);
            suite.addTestSuite(TestCookie.class);
            suite.addTestSuite(TestCheckUncheck.class);
            suite.addTestSuite(TestXPathLocators.class);
            suite.addTestSuite(TestClickJavascriptHref.class);
            suite.addTestSuite(TestCommandError.class);
            suite.addTestSuite(TestComments.class);
            suite.addTestSuite(TestFailingAssert.class);
            suite.addTestSuite(TestFailingVerifications.class);
            suite.addTestSuite(TestFocusOnBlur.class);
            suite.addTestSuite(TestGoBack.class);
            suite.addTestSuite(TestImplicitLocators.class);
            suite.addTestSuite(TestLocators.class);
            suite.addTestSuite(TestOpen.class);
            suite.addTestSuite(TestPatternMatching.class);
            suite.addTestSuite(TestPause.class);
            suite.addTestSuite(TestSelectWindow.class);
            suite.addTestSuite(TestStore.class);
            suite.addTestSuite(TestSubmit.class);
            suite.addTestSuite(TestType.class);
            suite.addTestSuite(TestVerifications.class);
            suite.addTestSuite(TestWait.class);
            suite.addTestSuite(TestSelect.class);
            suite.addTestSuite(TestEditable.class);
            suite.addTestSuite(TestPrompt.class);
            suite.addTestSuite(TestConfirmations.class);
            suite.addTestSuite(TestAlerts.class);
            suite.addTestSuite(TestRefresh.class);
            suite.addTestSuite(TestVisibility.class);
            suite.addTestSuite(TestMultiSelect.class);
            suite.addTestSuite(TestWaitInPopupWindow.class);
            suite.addTestSuite(TestWaitFor.class);
            suite.addTestSuite(TestWaitForNot.class);
            suite.addTestSuite(TestCssLocators.class);
            suite.addTestSuite(TestFramesClick.class);
            suite.addTestSuite(TestFramesOpen.class);
            suite.addTestSuite(TestFramesNested.class);
            suite.addTestSuite(TestClickBlankTarget.class);
            
            suite.addTestSuite(TestDojoDragAndDrop.class);
            suite.addTestSuite(TestDragAndDrop.class);
            suite.addTestSuite(TestElementIndex.class);
            suite.addTestSuite(TestElementOrder.class);
            suite.addTestSuite(TestElementPresent.class);
            
            
            suite.addTestSuite(TestFunkEventHandling.class);
            suite.addTestSuite(TestHighlight.class);
            suite.addTestSuite(TestHtmlSource.class);
            suite.addTestSuite(TestJavaScriptAttributes.class);
            suite.addTestSuite(TestOpenInTargetFrame.class);
            suite.addTestSuite(TestSelectMultiLevelFrame.class);
            suite.addTestSuite(TestSelectWindowTitle.class);
            suite.addTestSuite(TestTextWhitespace.class);
            
            suite.addTestSuite(TestEvilClosingWindow.class);
            
            if (isProxyInjectionMode) {
                suite.addTestSuite(MultiDomainTest.class);
                
                // Will need to run for IE tests
                //suite.addTestSuite(TestModalDialog.class);
            }
            
            if (!isProxyInjectionMode) {
                // SRC-324, TFCJH relies on out-of-frame effects, which are synchronous in JS mode but
                // asynchronous in PI mode, making this test unreliable
                suite.addTestSuite(TestFramesClickJavascriptHref.class);
                // In PI mode we force the browser to be *pi___, so we can't use *mock there
                suite.addTestSuite(MockBrowserTest.class);
                // SRC-312 TFST requires slide-up logic when the subframe is closed
                suite.addTestSuite(TestFramesSpecialTargets.class);
                // SRC-311 TTRT can't inject PI into iframe with no src
                suite.addTestSuite(TestTypeRichText.class);
                // SRC-330 TALS requires server-side persistence of locator strategies
                suite.addTestSuite(TestAddLocationStrategy.class);
            }
            
            if (false) {
                suite.addTestSuite(SSLOpenTest.class); // DGF this hangs the build; TODO investigate
                suite.addTestSuite(TestXPathLocatorInXHtml.class); // DGF firefox only
                suite.addTestSuite(TestCursorPosition.class); // DGF frequently fails on firefox
            }
            
            ClientDriverTestSetup setup = new ClientDriverTestSetup(suite);
            supersuite.addTest(setup);
            return supersuite;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
    


    /**
     * A TestSetup decorator that runs a super setUp and tearDown at the
     * beginning and end of the entire run: in this case, we use it to startup
     * and shutdown the in-process Selenium Server.
     * 
     * 
     * @author danielf
     * 
     */
    static class ClientDriverTestSetup extends TestSetup {
        SeleniumServer server;

        public ClientDriverTestSetup(Test test) {
            super(test);
        }

        public void setUp() throws Exception {
            try {
                server = new SeleniumServer();
                System.out.println("Starting the Selenium Server listening on port " + server.getPort()
                        + " as part of global setup...");
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        public void tearDown() throws Exception {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

    }
    
    /** 
     * A TestSetup decorator that runs a super setUp and tearDown at the
	 * beginning and end of the entire run.
	 *
	 * It is used to set system properties at the beginning of each run.
	 *
	 * @author nelsons
	 */
	static class InitSystemPropertiesTestSetup extends TestSetup {
		private HashMap/*<String, String>*/savedValuesOfSystemProperties = new HashMap/*<String, String>*/();

		public InitSystemPropertiesTestSetup(Test test) {
			super(test);
		}

		public void setUp() throws Exception {
			overrideProperty("selenium.debugMode", "true");
			overrideProperty("selenium.browserSideLog", "true");
			String logFile = "log.txt";
			File target = new File("target");
			if (target.exists() && target.isDirectory()) {
			    logFile = "target/log.txt";
			}
			overrideProperty("selenium.log", logFile);

			// make jetty logging especially verbose
			overrideProperty("DEBUG", "true");
			overrideProperty("DEBUG_VERBOSE", "1");
		}

		protected void overrideProperty(String propertyName,
				String propertyValue) {
			savedValuesOfSystemProperties.put(propertyName, System
					.getProperty(propertyName));
			System.setProperty(propertyName, propertyValue);
		}

		public void tearDown() throws Exception {
			restoreOldSystemPropertySettings();
		}

		private void restoreOldSystemPropertySettings() {
			for (Iterator i = savedValuesOfSystemProperties.keySet().iterator(); i
					.hasNext();) {
				String propertyName = (String) i.next();
				String oldValue = (String) savedValuesOfSystemProperties
						.get(propertyName);
				if (oldValue == null) {
					System.clearProperty(propertyName);
				} else {
					System.setProperty(propertyName, oldValue);
				}
			}
		}
	}
}
