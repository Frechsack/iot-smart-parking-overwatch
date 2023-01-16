package overwatch.service;

import overwatch.model.ProcessedZone;

public class RunForPixelsService {
    public static final byte RUNNER_EAST_NORTH = 0;
    public static final byte RUNNER_NORTH_EAST = 1;
    public static final byte RUNNER_NORTH_WEST = 2;
    public static final byte RUNNER_WEST_NORTH = 3;
    public static final byte RUNNER_SOUTH_WEST = 4;
    public static final byte RUNNER_WEST_SOUTH = 5;
    public static final byte RUNNER_SOUTH_EAST = 6;
    public static final byte RUNNER_EAST_SOUTH = 7;

    public static int[] walk(ProcessedZone currentZone, ProcessedZone[] zones, int x, int y, byte direction){
        int nextX;
        int nextY;

    ProcessedZone northZone;
    if(y == 1){
        for ()
    }
    boolean isGoingNorthPossible;

        if(direction == RUNNER_NORTH || direction == RUNNER_NORTH_EAST || direction == RUNNER_NORTH_WEST){
            nextY = y - 1;

        }

        if(direction == RUNNER_WEST || direction == RUNNER_NORTH_WEST || direction == RUNNER_SOUTH_WEST){
            nextX = x - 1;
        }

        if(direction == RUNNER_EAST || direction == RUNNER_NORTH_EAST || direction == RUNNER_SOUTH_EAST){
            nextX = x + 1;
        }

        if(direction == RUNNER_SOUTH || direction == RUNNER_SOUTH_EAST || direction == RUNNER_SOUTH_WEST){
            nextY = y + 1;
        }

    }



}
