package com.thoughtworks.selenium.generated;
import com.thoughtworks.selenium.*;
/**
 * @author XlateHtmlSeleneseToJava
 * Generated from //socrates/unixhome/nelsons/work/selenium-rc/trunk/javascript-core/src/selenium/tests/TestClickJavascriptHref.html.
 */
public class TestClickJavascriptHref extends SeleneseTestCase
{
   public void test() throws Throwable {
		selenium.setContext("Test Click", "info");
  
/* TestClickJavaScriptHref       */
		// open|./tests/html/test_click_javascript_page.html|
		selenium.open("./tests/html/test_click_javascript_page.html");

		/* Click a regular link */
		// click|link|
		selenium.click("link");
		// verifyAlert|link clicked|
		verifyEquals("link clicked", selenium.getAlert());

		/* Click link with multiple javascript calls */
		// click|linkWithMultipleJavascriptStatements|
		selenium.click("linkWithMultipleJavascriptStatements");
		// verifyAlert|alert1|
		verifyEquals("alert1", selenium.getAlert());
		// verifyAlert|alert2|
		verifyEquals("alert2", selenium.getAlert());
		// verifyAlert|alert3|
		verifyEquals("alert3", selenium.getAlert());

		/* Click a link with javascript:void() href */
		// click|linkWithJavascriptVoidHref|
		selenium.click("linkWithJavascriptVoidHref");
		// verifyAlert|onclick
		verifyEquals("onclick", selenium.getAlert());
		// verifyTitle|Click Page 1|
		verifyEquals("Click Page 1", selenium.getTitle());

		/* Click a link where onclick returns false */
		// click|linkWithOnclickReturnsFalse|
		selenium.click("linkWithOnclickReturnsFalse");
		// verifyTitle|Click Page 1|
		verifyEquals("Click Page 1", selenium.getTitle());

		/* No alert should be raised. */

		/* Click an image enclosed in a link */
		// click|enclosedImage|
		selenium.click("enclosedImage");
		// verifyAlert|enclosedImage clicked|
		verifyEquals("enclosedImage clicked", selenium.getAlert());

		checkForVerificationErrors();
	}
}
