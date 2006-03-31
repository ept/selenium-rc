package com.thoughtworks.selenium.generated;
import com.thoughtworks.selenium.*;
/**
 * @author XlateHtmlSeleneseToJava
 * Generated from //socrates/unixhome/nelsons/work/selenium-rc/trunk/javascript-core/src/selenium/tests/TestCommandError.html.
 */
public class TestCommandError extends SeleneseTestCase
{
   public void test() throws Throwable {
		selenium.setContext("Test Command Error", "info");
  
/* Test Command Error (should fail)       */
		// open|./tests/html/test_verifications.html|
		selenium.open("./tests/html/test_verifications.html");

		boolean sawThrow4 = false;
		try {
					// click|notALink|
		selenium.click("notALink");
		}
		catch (Throwable e) {
			sawThrow4 = true;
		}
		assertTrue(sawThrow4);
		

		boolean sawThrow6 = false;
		try {
					// select|noSuchSelect|somelabel
		selenium.select("noSuchSelect", "somelabel");
		}
		catch (Throwable e) {
			sawThrow6 = true;
		}
		assertTrue(sawThrow6);
		

		boolean sawThrow8 = false;
		try {
					// select|theSelect|label=noSuchLabel
		selenium.select("theSelect", "label=noSuchLabel");
		}
		catch (Throwable e) {
			sawThrow8 = true;
		}
		assertTrue(sawThrow8);
		

		boolean sawThrow10 = false;
		try {
					// select|theText|label=noSuchLabel
		selenium.select("theText", "label=noSuchLabel");
		}
		catch (Throwable e) {
			sawThrow10 = true;
		}
		assertTrue(sawThrow10);
		

		checkForVerificationErrors();
	}
}
