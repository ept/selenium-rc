package com.thoughtworks.selenium;

import org.testng.annotations.Test;

public class TestFifteenSecondSleep extends SeleneseTestNgHelper
{
   @Test
   public void testFifteenSecondSleep() throws Throwable {

        selenium.open("/selenium-server/tests/html/test_open.html");
        selenium.setContext("Sleeping 15 seconds");
        Thread.sleep(15000);
        selenium.open("/selenium-server/tests/html/test_open.html");        
    }
}
