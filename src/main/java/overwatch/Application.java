package overwatch;

import overwatch.debug.DebugFrame;
import overwatch.model.Capture;
import overwatch.model.Zone;
import overwatch.service.ImageService;

public class Application {

    public static void main (String[] args) {

        Capture c1 = new Capture(0,0,800,400, Capture.VIRTUAL_CAMERA_NAME);
        Zone z1 = new Zone(1, c1, 0,0, 200,200);
        Zone z2 = new Zone(2, c1, 200,0, 200,200);
        Zone z3 = new Zone(3, c1, 400,0, 200,200);
        Zone z4 = new Zone(4, c1,600,0, 200,200);
        Zone z5 = new Zone(5, c1,0,200, 200,200);
        Zone z6 = new Zone(6, c1,200,200, 200,200);
        Zone z7 = new Zone(7, c1,400,200, 200,200);
        Zone z8 = new Zone(8, c1,600,200, 200,200);

        Capture c2 = new Capture(0,400,800,400, Capture.VIRTUAL_CAMERA_NAME);
        Zone z9 = new Zone(9, c2, 0,0, 200,200);
        Zone z10 = new Zone(10, c2, 200,0, 200,200);
        Zone z11 = new Zone(11, c2, 400,0, 200,200);
        Zone z12 = new Zone(12, c2,600,0, 200,200);
        Zone z13 = new Zone(13, c2,0,200, 800,200);




        Zone[] zones = new Zone[]{
                z1,z2,z3,z4,z5,z6,z7,z8, z9, z10, z11, z12, z13
        };

        ImageService.updateSourceImage(c1);
        ImageService.updateCurrentImage(c1);
        ImageService.updateSourceImage(c2);
        ImageService.updateCurrentImage(c2);

         new DebugFrame(zones);




    }
}
