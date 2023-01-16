package overwatch.service;

import org.junit.Assert;
import org.junit.Test;
import overwatch.Constants;
import overwatch.model.Capture;
import overwatch.model.ProcessableZone;

public class ZoneServiceTest {

    @Test
    public void north(){
        Capture capture = new Capture(0, 0, 1920, 1080, Constants.VIRTUAL_CAMERA_NAME);
        ProcessableZone a = new ProcessableZone(1, 0,0,500, 300, capture);
        ProcessableZone b = new ProcessableZone(2, 500,0,500, 300, capture);
        ProcessableZone c = new ProcessableZone(3, 0,300,1000, 300, capture);
        ProcessableZone d = new ProcessableZone(4, 0,600,500, 300, capture);
        ProcessableZone e = new ProcessableZone(5, 500,600,500, 300, capture);
        ProcessableZone f = new ProcessableZone(6, 0,900,1000, 100, capture);
        ProcessableZone[] zones = new ProcessableZone[]{ a, b, c, d, e, f };

        Assert.assertEquals(d.nr, ZoneService.findZoneNorthOf(f, 10, zones).nr);
        Assert.assertEquals(b.nr, ZoneService.findZoneNorthOf(c, 500, zones).nr);
    }

    @Test
    public void south(){
        Capture capture = new Capture(0, 0, 1920, 1080, Constants.VIRTUAL_CAMERA_NAME);
        ProcessableZone a = new ProcessableZone(1, 0,0,500, 300, capture);
        ProcessableZone b = new ProcessableZone(2, 500,0,500, 300, capture);
        ProcessableZone c = new ProcessableZone(3, 0,300,1000, 300, capture);
        ProcessableZone d = new ProcessableZone(4, 0,600,500, 300, capture);
        ProcessableZone e = new ProcessableZone(5, 500,600,500, 300, capture);
        ProcessableZone f = new ProcessableZone(6, 0,900,1000, 100, capture);
        ProcessableZone[] zones = new ProcessableZone[]{ a, b, c, d, e, f };

        Assert.assertEquals(f.nr, ZoneService.findZoneSouthOf(d, 10, zones).nr);
        Assert.assertEquals(c.nr, ZoneService.findZoneSouthOf(b, 400, zones).nr);
        Assert.assertNull(ZoneService.findZoneSouthOf(f, 200, zones));
    }

    @Test
    public void east(){
        Capture capture = new Capture(0, 0, 1920, 1080, Constants.VIRTUAL_CAMERA_NAME);
        ProcessableZone a = new ProcessableZone(1, 0,0,500, 300, capture);
        ProcessableZone b = new ProcessableZone(2, 500,0,500, 300, capture);
        ProcessableZone c = new ProcessableZone(3, 0,300,1000, 300, capture);
        ProcessableZone d = new ProcessableZone(4, 0,600,500, 300, capture);
        ProcessableZone e = new ProcessableZone(5, 500,600,500, 300, capture);
        ProcessableZone f = new ProcessableZone(6, 0,900,1000, 100, capture);
        ProcessableZone[] zones = new ProcessableZone[]{ a, b, c, d, e, f };

        Assert.assertEquals(b.nr, ZoneService.findZoneEastOf(a, 10, zones).nr);
        Assert.assertEquals(e.nr, ZoneService.findZoneEastOf(d, 200, zones).nr);
        Assert.assertNull(ZoneService.findZoneEastOf(f, 50, zones));
    }

    @Test
    public void west(){
        Capture capture = new Capture(0, 0, 1920, 1080, Constants.VIRTUAL_CAMERA_NAME);
        ProcessableZone a = new ProcessableZone(1, 0,0,500, 300, capture);
        ProcessableZone b = new ProcessableZone(2, 500,0,500, 300, capture);
        ProcessableZone c = new ProcessableZone(3, 0,300,1000, 300, capture);
        ProcessableZone d = new ProcessableZone(4, 0,600,500, 300, capture);
        ProcessableZone e = new ProcessableZone(5, 500,600,500, 300, capture);
        ProcessableZone f = new ProcessableZone(6, 0,900,1000, 100, capture);
        ProcessableZone[] zones = new ProcessableZone[]{ a, b, c, d, e, f };

        Assert.assertEquals(a.nr, ZoneService.findZoneWestOf(b, 10, zones).nr);
        Assert.assertEquals(d.nr, ZoneService.findZoneWestOf(e, 200, zones).nr);
        Assert.assertNull(ZoneService.findZoneWestOf(f, 50, zones));
    }
}
