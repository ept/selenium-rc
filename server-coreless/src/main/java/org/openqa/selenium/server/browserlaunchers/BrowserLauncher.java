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

import org.openqa.selenium.server.BrowserConfigurationOptions;

/**
 * The launcher interface for classes that will start/stop the browser process.
 *
 * @author Paul Hammant
 * @version $Revision: 732 $
 */
public interface BrowserLauncher {
    /**
     * Start the browser and navigate directly to the specified URL
     *
     * @param multiWindow TODO
     */
    void launchRemoteSession(String url, boolean multiWindow);
    
    /**
     * Start the browser and navigate directly to the specified URL using the supplied browser configurations
     *
     * @param multiWindow a flag to specify multiWindow mode
     * @param browserConfigurationOptions Test level browser configuration object
     */
    void launchRemoteSession(String url, boolean multiWindow, BrowserConfigurationOptions browserConfigurationOptions);

    /**
     * Start the browser in Selenese mode, auto-running the specified HTML suite
     *
     * @param startURL    the url within which to initiate the session (if needed)
     * @param suiteUrl    the url of the HTML suite to launch
     * @param multiWindow TODO
     * @param defaultLogLevel TODO
     */
    void launchHTMLSuite(String startURL, String suiteUrl, boolean multiWindow, String defaultLogLevel);

    /**
     * Stop (kill) the browser process
     */
    void close();

    /**
     * Returns a process if there is an associated one running with this browser launcher (this is <b>not</b> required to be implementd).
     *
     * @return a handle to a process if one is available, or null if one is not available or if no browser is running
     */
    Process getProcess();

}
