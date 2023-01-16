package overwatch.service;

import overwatch.model.ProcessableZone;

public class RunForPixelsService {
    public static final byte RUNNER_EAST_NORTH = 0;
    public static final byte RUNNER_NORTH_EAST = 1;
    public static final byte RUNNER_NORTH_WEST = 2;
    public static final byte RUNNER_WEST_NORTH = 3;
    public static final byte RUNNER_SOUTH_WEST = 4;
    public static final byte RUNNER_WEST_SOUTH = 5;
    public static final byte RUNNER_SOUTH_EAST = 6;
    public static final byte RUNNER_EAST_SOUTH = 7;




    public static int[] walk(ProcessableZone currentZone, ProcessableZone[] zones, int x, int y, byte direction){
        int nextX;
        int nextY;

        // zoneNr, x, y
        return new int[2];

    }

}
