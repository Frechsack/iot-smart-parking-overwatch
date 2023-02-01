package overwatch;

import overwatch.debug.DebugFrame;
import overwatch.model.Capture;
import overwatch.model.Zone;
import overwatch.service.ConfigurationService;

public class DebugApplication {

    public static void main(String[] args) {
        ConfigurationService.override(ConfigurationService.Keys.DEBUG_FRAME_ENABLE, "true");

        // 640 x 480
        Capture c1 = new Capture(0,0,640,480, "/dev/video0");

       // Zone z1 = new Zone(1, c1,   40,  0, 120,160);
        Zone z2 = new Zone(2, c1, 200,  100, 110,60);
        Zone z3 = new Zone(3, c1, 310,  100, 110,60);
       // Zone z4 = new Zone(4, c1, 420,  0,  120,160);

        Zone z5 = new Zone(5, c1,60,200, 140,100);
        Zone z6 = new Zone(6, c1,200,160, 120,150);
        Zone z7 = new Zone(7, c1,320,160, 110,146);
        Zone z8 = new Zone(8, c1,430,200, 120,100);

        //Zone z9 = new Zone(9, c1,20,320, 140,160);
        //Zone z10 = new Zone(10, c1,160,320, 130,160);
        //Zone z11 = new Zone(11, c1,290,320, 130,160);
        //Zone z12 = new Zone(12, c1,420,320, 120,160);

        Zone[] zones = new Zone[]{
                z2,z3,z5,z6,z7, z8, //z9, z10, z11, z12
        };

        Engine.start(zones);
    }
}
