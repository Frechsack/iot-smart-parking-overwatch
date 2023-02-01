package overwatch.skeleton;

import org.junit.Assert;
import org.junit.Test;
import overwatch.model.Capture;
import overwatch.model.Zone;

public class OutlineTest {

    @Test
    public void findOutlineForPosition() {
        Capture capture = new Capture(0,0,200,200,Capture.VIRTUAL_CAMERA_NAME);
        Zone z1 =  new Zone(1,capture,0,0,100,100);
        Zone z2 =  new Zone(2,capture,0,100,100,100);
        Zone[] zones = new Zone[] {z1, z2};
        Assert.assertEquals(z1, Outline.findOutlineForPosition(50,50,zones));
    }

    @Test
    public void intersectionArea() {
        Capture capture = new Capture(0,0,200,200,Capture.VIRTUAL_CAMERA_NAME);
        Zone z1 =  new Zone(1,capture,0,0,100,100);
        Zone z2 =  new Zone(2,capture,20,0,100,100);
        Assert.assertEquals(8000, Outline.intersectionArea(z1,z2));
    }

    @Test
    public void isIntersecting() {
        Capture capture = new Capture(0,0,200,200,Capture.VIRTUAL_CAMERA_NAME);
        Zone z1 =  new Zone(1,capture,0,0,100,100);
        Zone z2 =  new Zone(2,capture,20,0,100,100);
        Assert.assertTrue( Outline.isIntersecting(z1,z2));
        Assert.assertTrue( Outline.isIntersecting(z1,z2,100 ));

        Zone z3 =  new Zone(1,capture,0,0,10,100);
        Zone z4 =  new Zone(2,capture,20,0,100,100);
        Assert.assertFalse(Outline.isIntersecting(z3,z4));
        Assert.assertTrue(Outline.isIntersecting(z3,z4,10 ));
        Assert.assertFalse(Outline.isIntersecting(z3,z4,4 ));
    }


    @Test
    public void compose() {
        Outline a = Outline.of(0,0,20,20);
        Outline b = Outline.of(30,0,20,20);
        Outline c = Outline.compose(a,b);
        Assert.assertEquals(0,c.x());
        Assert.assertEquals(0,c.y());
        Assert.assertEquals(50,c.width());
        Assert.assertEquals(20,c.height());
        Assert.assertEquals(49,c.endX());
        Assert.assertEquals(19,c.endY());
    }

}