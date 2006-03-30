package com.thoughtworks.selenium.generated;
import com.thoughtworks.selenium.*;
/**
 * @author XlateHtmlSeleneseToJava
 * Generated from //socrates/unixhome/nelsons/work/selenium-rc/trunk/javascript-core/src/selenium/tests/TestGoBack.html.
 */
public class TestGoBack extends SeleneseTestCase
{
   public void test() throws Throwable {
		selenium.setContext("Test Back and Forward", "info");
  
/* Test Back and Forward       */
			// open|./tests/html/test_click_page1.html|
			selenium.open("./tests/html/test_click_page1.html");
			// verifyTitle|Click Page 1|
			verifyEquals("Click Page 1", selenium.getTitle());

		/* Click a regular link */
			// clickAndWait|link|
			selenium.click("link");
		selenium.waitForPageToLoad("60000");
			// verifyTitle|Click Page Target|
			verifyEquals("Click Page Target", selenium.getTitle());
			// goBackAndWait||
			selenium.goBack();
		selenium.waitForPageToLoad("60000");
			// verifyTitle|Click Page 1|
			verifyEquals("Click Page 1", selenium.getTitle());

		/* history.forward() generates 'Permission Denied' in IE     >>>>>goForward////////////<<<<<
			// verifyTitle|Click Page Target|
			verifyEquals("Click Page Target", selenium.getTitle());
 */

		checkForVerificationErrors();
	}
}