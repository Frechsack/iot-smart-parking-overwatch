package overwatch.service;

import org.junit.Assert;
import org.junit.Test;
import overwatch.Constants;
import overwatch.model.Capture;
import overwatch.model.ProcessableZone;

public class RunForPixelServiceTest {

    @Test
    public void walkRight(){
        Capture capture = new Capture(0,0, 100, 100, Constants.VIRTUAL_CAMERA_NAME);
        ProcessableZone a = new ProcessableZone(1,0,0, 20,100, capture);
        ProcessableZone b = new ProcessableZone(1,20,0, 20,100, capture);
        ProcessableZone c = new ProcessableZone(1,40,0, 20,100, capture);
        ProcessableZone d = new ProcessableZone(1,60,0, 20,100, capture);
        ProcessableZone e = new ProcessableZone(1,80,0, 20,100, capture);
        ProcessableZone[] zones = new ProcessableZone[]{ a,b,c,d,e };
        ImageService.updateCurrentImage(capture);
        ImageService.updateSourceImage(capture);


        Assert.assertEquals(60, RunForPixelsService.walkRight(5, 60, zones, d, d));


    }


}
