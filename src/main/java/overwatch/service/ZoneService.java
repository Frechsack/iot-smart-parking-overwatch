package overwatch.service;

import overwatch.model.ProcessableZone;
import overwatch.model.Zone;

public class ZoneService {

    public enum PixelState {

        MODIFIED(true, false),

        UNMODIFIED(false, false),

        MODIFIED_ZONE_CHANGE(true, true),

        UNMODIFIED_ZONE_CHANGE(false, true);

        public final boolean isModified;

        public final boolean isZoneChanged;

        PixelState(boolean isModified, boolean isZoneChanged){
            this.isZoneChanged = isZoneChanged;
            this.isModified = isModified;
        }
    }

    private static final int INTERSECTION_DELTA = 5;

    private ZoneService () {}


    public static <E extends Zone> E findZoneForPixel(final int absoluteX, final int absoluteY, final E[] zones ){
        for(E zone : zones) {
            if (zone.absoluteXStart <= absoluteX && zone.absoluteXEnd > absoluteX && zone.absoluteYStart <= absoluteY && zone.absoluteYEnd > absoluteY)
                return zone;
        }
        throw new IllegalArgumentException("There is no matching zone for coordinates x: '" + absoluteX + "' y: '" + absoluteY + "'");
    }


    /**
     * Prüft, ob ein Pixel mutiert ist.
     * @param absoluteX Die absolute Position des Pixels auf der x-Achse.
     * @param absoluteY Die absolute Position des Pixels auf der y-Achse.
     * @param zones Die auszuwertenden Zonen.
     * @param shortcut Eine optionale Zone, die zuerst durchsucht werden soll.
     * @return Gibt zurück, ob der Pixel mutiert wurde oder nicht.
     * @param <E> Class-type von {@link Zone}.
     */
    public static PixelState calculatePixelState(final int absoluteX, final int absoluteY, final ProcessableZone[] zones, final ProcessableZone shortcut){
        if(shortcut != null && shortcut.absoluteXStart <= absoluteX && shortcut.absoluteXEnd > absoluteX && shortcut.absoluteYStart <= absoluteY && shortcut.absoluteYEnd > absoluteY)
            return shortcut.isModified(absoluteX - shortcut.absoluteXStart, absoluteY - shortcut.absoluteYStart)
                ? PixelState.MODIFIED
                : PixelState.UNMODIFIED;

        for(ProcessableZone zone : zones) {
            if (zone.absoluteXStart <= absoluteX && zone.absoluteXEnd > absoluteX && zone.absoluteYStart <= absoluteY && zone.absoluteYEnd > absoluteY)
                return zone.isModified(absoluteX - zone.absoluteXStart, absoluteY - zone.absoluteYStart)
                        ? PixelState.MODIFIED_ZONE_CHANGE
                        : PixelState.UNMODIFIED_ZONE_CHANGE;
        }
        throw new IllegalArgumentException("There is no matching zone for coordinates x: '" + absoluteX + "' y: '" + absoluteY + "'");
    }

    public static <E extends Zone> E findZoneNorthOf(E currentZone, int x, E[] zones) {
        final int absoluteX = currentZone.absoluteXStart + x;
        assert absoluteX <= currentZone.absoluteXEnd;
        for (E zone : zones){
            if(zone == currentZone) continue;
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
            if(zone == currentZone) continue;
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
            if(zone == currentZone) continue;
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
            if(zone == currentZone) continue;
            final int xDifference = zone.absoluteXEnd - currentZone.absoluteXStart;
            if(Math.abs(xDifference) > INTERSECTION_DELTA) continue;
            if(absoluteY > zone.absoluteYEnd || absoluteY < zone.absoluteYStart) continue;
            return zone;
        }
        return null;
    }

}
