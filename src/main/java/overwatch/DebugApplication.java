package overwatch;

import overwatch.debug.DebugFrame;
import overwatch.model.Capture;
import overwatch.model.Zone;
import overwatch.service.ImageService;

public class DebugApplication {

    public static void main(String[] args) {
        Capture c1 = new Capture(0,0,352,288, "/dev/video0");
        Zone z1 = new Zone(1, c1,   0,  0, 100,150);
        Zone z2 = new Zone(2, c1, 100,  0, 100,150);
        Zone z3 = new Zone(3, c1, 200,  0, 100,150);
        Zone z4 = new Zone(4, c1, 300,  0,  52,150);

        Zone z5 = new Zone(5, c1,0,150, 100,138);
        Zone z6 = new Zone(6, c1,100,150, 100,138);
        Zone z7 = new Zone(7, c1,200,150, 100,138);
        Zone z8 = new Zone(7, c1,300,150, 52,138);

        Zone[] zones = new Zone[]{
                z1,z2,z3,z4,z5,z6,z7, z8
        };

        Engine.start(zones);

        new DebugFrame(zones);
    }
}
