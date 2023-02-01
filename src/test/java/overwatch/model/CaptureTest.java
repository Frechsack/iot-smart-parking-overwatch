package overwatch.model;

import org.junit.Assert;
import org.junit.Test;

public class CaptureTest {

    @Test
    public void isVirtual() {
        Capture capture = new Capture(0,0,100,100,"/dev/video0");
        Assert.assertFalse(capture.isVirtual());

        capture = new Capture(0,0,100,100,Capture.VIRTUAL_CAMERA_NAME);
        Assert.assertTrue(capture.isVirtual());
    }
}