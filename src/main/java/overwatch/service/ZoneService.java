package overwatch.service;

import overwatch.model.ProcessableZone;
import overwatch.model.Zone;

import java.util.Map;

public class ZoneService {

    private static final int INTERSECTION_DELTA = 5;

    private ZoneService () {}

    public static <E extends Zone> E findZoneNorthOf(E currentZone, int x, E[] zones) {
        final int absoluteX = currentZone.absoluteXStart + x;
        assert absoluteX <= currentZone.absoluteXEnd;
        for (E zone : zones){
            final int yDifference = currentZone.absoluteYStart - zone.absoluteYEnd;
            if(Math.abs(yDifference) > INTERSECTION_DELTA) continue;
            if(absoluteX > zone.absoluteXEnd || absoluteX < zone.absoluteXStart) continue;
            return zone;
        }
        return null;
    }

    public static <E extends Zone> E findZoneSouthOf(E currentZone, int x, E[] zones) {
        final int absoluteX = currentZone.absoluteXStart + x;
        assert absoluteX <= currentZone.absoluteXEnd;

        for (E zone : zones){
            final int yDifference = zone.absoluteYStart - currentZone.absoluteYEnd;
            if(Math.abs(yDifference) > INTERSECTION_DELTA) continue;
            if(absoluteX > zone.absoluteXEnd || absoluteX < zone.absoluteXStart) continue;
            return zone;
        }
        return null;
    }

    public static <E extends Zone> E findZoneEastOf(E currentZone, int y, E[] zones) {
        final int absoluteY = currentZone.absoluteYStart + y;
        assert absoluteY <= currentZone.absoluteYEnd;

        for(E zone : zones) {
            final int xDifference = zone.absoluteXStart - currentZone.absoluteXEnd;
            if(Math.abs(xDifference) > INTERSECTION_DELTA) continue;
            if(absoluteY > zone.absoluteYEnd || absoluteY < zone.absoluteYStart) continue;
            return zone;
        }
        return null;
    }

    public static <E extends Zone> E findZoneWestOf(E currentZone, int y, E[] zones) {
        final int absoluteY = currentZone.absoluteYStart + y;
        assert absoluteY <= currentZone.absoluteYEnd;

        for(E zone : zones) {
            final int xDifference = zone.absoluteXEnd - currentZone.absoluteXStart;
            if(Math.abs(xDifference) > INTERSECTION_DELTA) continue;
            if(absoluteY > zone.absoluteYEnd || absoluteY < zone.absoluteYStart) continue;
            return zone;
        }
        return null;
    }

}
