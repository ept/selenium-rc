package org.openqa.selenium.server.browserlaunchers;

import org.openqa.selenium.server.BrowserConfigurationOptions;
import org.openqa.selenium.server.RemoteControlConfiguration;

/**
 * launcher for Firefox under proxy injection mode
 * 
 * In proxy injection mode, the selenium server is a proxy for all traffic from the browser, 
 * not just traffic going to selenium-server URLs.  The incoming HTML is modified 
 * to include selenium's JavaScript, which then controls the test page from within (as 
 * opposed to controlling the test page from a different window, as selenium remote 
 * control normally does).
 * 
 * @author nelsons
 *
 */
public class ProxyInjectionFirefoxCustomProfileLauncher extends
        FirefoxCustomProfileLauncher {

    private static boolean alwaysChangeMaxConnections = true;

    public ProxyInjectionFirefoxCustomProfileLauncher(BrowserConfigurationOptions browserOptions,
            RemoteControlConfiguration configuration, String sessionId, String browserLaunchLocation) {
        super(browserOptions, configuration, sessionId, browserLaunchLocation);
    }

    @Override
    protected void init() {
        super.init();
        changeMaxConnections = alwaysChangeMaxConnections;
        proxySetting = LauncherUtils.ProxySetting.PROXY_EVERYTHING;
    }
    
    public static void setChangeMaxConnections(boolean changeMaxConnections) {
    	ProxyInjectionFirefoxCustomProfileLauncher.alwaysChangeMaxConnections = changeMaxConnections;
    }
}
