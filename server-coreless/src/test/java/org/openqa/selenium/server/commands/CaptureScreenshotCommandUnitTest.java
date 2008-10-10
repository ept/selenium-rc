package org.openqa.selenium.server.commands;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import junit.framework.TestCase;
import org.easymock.classextension.ConstructorArgs;
import static org.easymock.classextension.EasyMock.*;

public class CaptureScreenshotCommandUnitTest extends TestCase {

	private CaptureScreenshotCommand command;
	private String fileName = "test_screenshot.png";
	private File file = new File(fileName);
	private String tempDirName = System.getProperty("java.io.tmpdir"); 
	
    public void disable_testExecuteReturnsOKWhencaptureSystemScreenshotSucceeds() throws Exception {
        final ConstructorArgs args;

        args = new ConstructorArgs(CaptureScreenshotCommand.class.getConstructor(String.class), fileName);
        command = createMock(CaptureScreenshotCommand.class,
                             args,
                             CaptureScreenshotCommand.class.getDeclaredMethod("captureSystemScreenshot"));
        command.captureSystemScreenshot();

        replay(command);
        assertEquals("OK", command.execute());
        verify(command);
    }

    public void disable_testExecuteReturnsAnErrorWhencaptureSystemScreenshotRaise() throws Exception {

        final ConstructorArgs args;

        args = new ConstructorArgs(CaptureScreenshotCommand.class.getConstructor(String.class), fileName);
        command = createMock(CaptureScreenshotCommand.class,
                             args,
                             CaptureScreenshotCommand.class.getDeclaredMethod("captureSystemScreenshot"));
        command.captureSystemScreenshot();
        expectLastCall().andThrow(new RuntimeException("an error message"));
        replay(command);

        assertEquals("ERROR: Problem capturing screenshot: an error message", command.execute());
        verify(command);
    }

    // TODO: Mock File, Robot and ImageIO.write to reduce execution time
    
    public void disable_testTakingScreenshotToSingleFileNameCreatesScreenshotInWorkingDirectory() throws Exception {
    	command = new CaptureScreenshotCommand(file);
    	assertEquals("OK", command.execute());
    	assertTrue(file.exists());
    }
    
    public void disable_testTakingScreenshotToAbsolutePathWithExistingComponentsCreatesScreenshot() throws Exception {
    	file = new File(tempDirName + File.separator + fileName);
    	command = new CaptureScreenshotCommand(file);
    	assertEquals("OK", command.execute());
    	assertTrue(file.exists());
    }
    
    public void disable_testTakingScreenshotToAbsolutePathWithPartiallyExistingComponentsCreatesNecessaryDirectories() throws Exception {
    	file = new File(tempDirName + File.separator + "toBeCreated" + File.separator + fileName);
    	command = new CaptureScreenshotCommand(file);
    	assertEquals("OK", command.execute());
    	assertTrue(file.exists());
    }
    
    public void disable_testScreenshotIsValidImage() throws Exception {
    	disable_testTakingScreenshotToSingleFileNameCreatesScreenshotInWorkingDirectory();
    	BufferedImage image = ImageIO.read(file);
    	assertNotNull(image);
    }
    
    public void tearDown() throws Exception {
    	if (file.exists()) {
			file.delete();
		}
		if (file.getParentFile() != null && file.getParentFile().exists()) {
			file.getParentFile().delete();
		}
    }
}
