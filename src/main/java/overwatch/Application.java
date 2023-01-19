package overwatch;

import overwatch.debug.DebugFrame;
import overwatch.model.Capture;
import overwatch.model.Zone;
import overwatch.service.ImageService;

public class Application {

    public static void main (String[] args) {

        Capture capture = new Capture(0,0,800,400, Capture.VIRTUAL_CAMERA_NAME);
        Zone z1 = new Zone(1, capture, 0,0, 200,200);
        Zone z2 = new Zone(2, capture, 200,0, 200,200);
        Zone z3 = new Zone(3, capture, 400,0, 200,200);
        Zone z4 = new Zone(4, capture,600,0, 200,200);
        Zone z5 = new Zone(5, capture,0,200, 200,200);
        Zone z6 = new Zone(6, capture,200,200, 200,200);
        Zone z7 = new Zone(7, capture,400,200, 200,200);
        Zone z8 = new Zone(8, capture,600,200, 200,200);

        Zone[] zones = new Zone[]{
                z1,z2,z3,z4,z5,z6,z7,z8
        };

        ImageService.updateSourceImage(capture);
        ImageService.updateCurrentImage(capture);

        DebugFrame debugFrame = new DebugFrame(zones);




    }
}
