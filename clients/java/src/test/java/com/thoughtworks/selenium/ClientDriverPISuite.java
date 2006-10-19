package com.thoughtworks.selenium;

import junit.framework.Test;
import org.openqa.selenium.server.browserlaunchers.WindowsUtils;

/**
 * This class executes the same tests that ClientDriverSuite does, but it does so with proxy injection mode
 * turned on and with the default browser string set so as to use IE.  (ClientDriverSuite normally uses Firefox.)
 * 
 * @author nelsons
 *
 */

public class ClientDriverPISuite extends ClientDriverSuite {
    public static Test suite() {
        System.setProperty("selenium.proxyInjectionMode", "true");
        if (WindowsUtils.thisIsWindows()) {
            System.setProperty("selenium.defaultBrowserString", "*piiexplore");
        } else {
            System.setProperty("selenium.defaultBrowserString", "*pifirefox");
        }

        return ClientDriverSuite.suite();
    }
}
