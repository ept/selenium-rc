/*
 * Copyright 2006 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.openqa.selenium.server.browserlaunchers;

import java.io.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.openqa.selenium.server.*;

public class FirefoxCustomProfileLauncher extends DestroyableRuntimeExecutingBrowserLauncher {

    private static final String DEFAULT_LOCATION = "c:\\program files\\mozilla firefox\\firefox.exe"; 
    
    private int port = 8180; 
    private File customProfileDir;
    private String[] cmdarray;
    
    public FirefoxCustomProfileLauncher() {
        super(findBrowserLaunchLocation());
    }
    
    public FirefoxCustomProfileLauncher(int port) {
        super(findBrowserLaunchLocation());
        this.port = port;
    }
    
    public FirefoxCustomProfileLauncher(int port, String browserLaunchLocation) {
        super(browserLaunchLocation);
        this.port = port;
    }
    
    private static String findBrowserLaunchLocation() {
        String defaultPath = System.getProperty("firefoxDefaultPath", DEFAULT_LOCATION);
        File defaultLocation = new File(defaultPath);
        if (defaultLocation.exists()) {
            return defaultLocation.getAbsolutePath();
        } else {
            // Hope it's on the path
            return "firefox";
        }
    }
    
    public void launch(String url) {
        try {
            String profilePath = makeCustomProfile();
            
            cmdarray = new String[] {commandPath, "-profile", profilePath, url};
            
            AsyncExecute exe = new AsyncExecute();
            exe.setCommandline(cmdarray);
            exe.setEnvironment(new String[] {"MOZ_NO_REMOTE=1"});
            // Set MOZ_NO_REMOTE in order to ensure we always get a new Firefox process
            // http://blog.dojotoolkit.org/2005/12/01/running-multiple-versions-of-firefox-side-by-side
            process = exe.asyncSpawn();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    

    private String makeCustomProfile() throws IOException {
        customProfileDir = new File("customProfileDir");
        if (customProfileDir.exists()) {
            recursivelyDeleteDir(customProfileDir);
        }
        customProfileDir.mkdir();
        
        File proxyPAC = new File(customProfileDir, "proxy.pac");
        PrintStream out = new PrintStream(new FileOutputStream(proxyPAC));
        out.println("function FindProxyForURL(url, host) {");
        out.println("   if(shExpMatch(url, '*/selenium-server/*')) {");
        out.println("       return 'PROXY localhost:" + Integer.toString(port) + "; DIRECT'");
        out.println("   }");
        out.println("}");
        out.close();
        
        // TODO Do we want to make these preferences configurable somehow?
        // TODO Disable pop-up blocking?
        File prefsJS = new File(customProfileDir, "prefs.js");
        out = new PrintStream(new FileOutputStream(prefsJS));
        // Don't ask if we want to switch default browsers
        out.println("user_pref('browser.shell.checkDefaultBrowser', false);");
        
        // Configure us as the local proxy
        out.println("user_pref('network.proxy.type', 2);");
        out.println("user_pref('network.proxy.autoconfig_url', '" +
                proxyPAC.toURL() + 
                "');");
        
        // Disable security warnings
        out.println("user_pref('security.warn_submit_insecure', false);");
        out.println("user_pref('security.warn_submit_insecure.show_once', false);");
        out.println("user_pref('security.warn_entering_secure', false);");
        out.println("user_pref('security.warn_entering_secure.show_once', false);");
        out.println("user_pref('security.warn_entering_weak', false);");
        out.println("user_pref('security.warn_entering_weak.show_once', false);");
        out.println("user_pref('security.warn_leaving_secure', false);");
        out.println("user_pref('security.warn_leaving_secure.show_once', false);");
        out.println("user_pref('security.warn_viewing_mixed', false);");
        out.println("user_pref('security.warn_viewing_mixed.show_once', false);");
        
        // Disable "do you want to remember this password?"
        out.println("user_pref('signon.rememberSignons', false);");
        out.close();
        return customProfileDir.getAbsolutePath();
    }

    public void close() {
        Exception taskKillException = null;
        if (WindowsUtils.thisIsWindows()) {
            try {
                // try to kill with windows taskkill
                WindowsUtils.kill(cmdarray);
            } catch (Exception e) {
                taskKillException = e;
            }
        }
        super.close();
        /* Sleeping two seconds to give Windows time to
         * notice that the cache files in the customProfileDir
         * are now unlocked
         */
        try {Thread.sleep(2000);} catch (InterruptedException e) {}
        try {
            recursivelyDeleteDir(customProfileDir);
        } catch (RuntimeException e) {
            if (taskKillException != null) {
                e.printStackTrace();
                System.err.print("Perhaps caused by: ");
                taskKillException.printStackTrace();
                throw new RuntimeException("Couldn't delete custom Firefox " +
                        "profile directory, presumably because task kill failed; " +
                        "see stderr!", e);
            }
            throw e;
        }
    }
    
    private void recursivelyDeleteDir(File f) {
        Delete delete = new Delete();
        delete.setProject(new Project());
        delete.setDir(customProfileDir);
        delete.setFailOnError(true);
        delete.execute();
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        FirefoxCustomProfileLauncher l = new FirefoxCustomProfileLauncher(SeleniumServer.DEFAULT_PORT);
        l.launch("http://www.google.com");
        int seconds = 15;
        System.out.println("Killing browser in " + Integer.toString(seconds) + " seconds");
        Thread.sleep(seconds * 1000);
        l.close();
        System.out.println("He's dead now, right?");
    }
    
}
