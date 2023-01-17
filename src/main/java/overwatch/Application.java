package overwatch;

import overwatch.debug.DebugFrame;
import overwatch.model.Capture;
import overwatch.model.Zone;
import overwatch.service.ImageService;

public class Application {

    public static void main (String[] args) {



        Capture capture = new Capture(0,0,800,400, Capture.VIRTUAL_CAMERA_NAME);
        Zone z1 = new Zone(1, 0,0, 200,200, capture);
        Zone z2 = new Zone(2, 200,0, 200,200, capture);
        Zone z3 = new Zone(3, 400,0, 200,200, capture);
        Zone z4 = new Zone(4, 600,0, 200,200, capture);
        Zone z5 = new Zone(5, 0,200, 200,200, capture);
        Zone z6 = new Zone(6, 200,200, 200,200, capture);
        Zone z7 = new Zone(7, 400,200, 200,200, capture);
        Zone z8 = new Zone(8, 600,200, 200,200, capture);

        Zone[] zones = new Zone[]{
                z1,z2,z3,z4,z5,z6,z7,z8
        };

        ImageService.updateSourceImage(capture);
        ImageService.updateCurrentImage(capture);

        DebugFrame debugFrame = new DebugFrame(capture, zones);




    }
}
