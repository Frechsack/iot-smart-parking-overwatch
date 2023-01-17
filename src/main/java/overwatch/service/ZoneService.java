package overwatch.service;

import org.jetbrains.annotations.Nullable;
import overwatch.model.ProcessableZone;
import overwatch.model.Zone;

/**
 * Statische Methoden für die Berechnung von Zonen übergreifenden Operationen.
 */
public class ZoneService {

    /**
     * Die maximale Überschneidung bzw. Abstand zwischen zwei Zonen damit diese berührend erkannt werden.
     */
    private static final int INTERSECTION_DELTA = 5;

    /**
     * Der Zustand eines Pixels.
     */
    public enum PixelState {

        /**
         * Der Pixel wurde gefunden und modifiziert.
         */
        MODIFIED(true, false, true),

        /**
         * Der Pixel wurde gefunden und nicht modifiziert.
         */
        UNMODIFIED(false, false, true),

        /**
         * Der Pixel wurde gefunden und modifiziert. Der Pixel wurde außerdem in einer anderen Zone gefunden, als angenommen.
         */
        MODIFIED_ZONE_CHANGE(true, true, true),

        /**
         * Der Pixel wurde gefunden und nicht modifiziert. Der Pixel wurde außerdem in einer anderen Zone gefunden, als angenommen.
         */
        UNMODIFIED_ZONE_CHANGE(false, true, true),

        /**
         * Der Pixel wurde in keiner Zone gefunden.
         */
        NOT_EXISTING(false, false, false);

        public final boolean isModified;

        public final boolean isZoneChanged;

        public final boolean isExisting;

        PixelState(boolean isModified, boolean isZoneChanged, boolean isExisting){
            this.isZoneChanged = isZoneChanged;
            this.isModified = isModified;
            this.isExisting = isExisting;
        }
    }

    private ZoneService () {}

    /**
     * Errechnet die Zone zu einem Pixel.
     * @param absoluteX Die absolute Position des Pixel auf der x-Achse.
     * @param absoluteY Die absolute Position des Pixel auf der y-Achse.
     * @param zones Die zu analysierenden Zonen.
     * @return Gibt die errechnete Zone zurück..
     * @param <E> Der class-type.
     * @throws IllegalArgumentException Wirft eine {@link IllegalArgumentException}, sollte keine Zone gefunden werden.
     */
    public static <E extends Zone> E findZoneForPixel(final int absoluteX, final int absoluteY, final E[] zones ){
        for(E zone : zones) {
            if (zone.absoluteXStart <= absoluteX && zone.absoluteXEnd >= absoluteX && zone.absoluteYStart <= absoluteY && zone.absoluteYEnd >= absoluteY)
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
     */
    public static PixelState calculatePixelState(final int absoluteX, final int absoluteY, final ProcessableZone[] zones,@Nullable final ProcessableZone shortcut){
        if(shortcut != null && shortcut.absoluteXStart <= absoluteX && shortcut.absoluteXEnd >= absoluteX && shortcut.absoluteYStart <= absoluteY && shortcut.absoluteYEnd >= absoluteY)
            return shortcut.isModified(absoluteX - shortcut.absoluteXStart, absoluteY - shortcut.absoluteYStart)
                ? PixelState.MODIFIED
                : PixelState.UNMODIFIED;

        for(ProcessableZone zone : zones) {
            if (zone.absoluteXStart <= absoluteX && zone.absoluteXEnd >= absoluteX && zone.absoluteYStart <= absoluteY && zone.absoluteYEnd >= absoluteY)
                return zone.isModified(absoluteX - zone.absoluteXStart, absoluteY - zone.absoluteYStart)
                        ? PixelState.MODIFIED_ZONE_CHANGE
                        : PixelState.UNMODIFIED_ZONE_CHANGE;
        }
        return PixelState.NOT_EXISTING;
    }

    /**
     * Findet eine Zone, welche Nördlich der übergebenen Zone liegt.
     * @param currentZone Die ausgangs Zone.
     * @param relativeX Die Position auf der x-Achse. Die Nördlich liegende Zone muss die x-Achse an dieser Koordinate schneiden.
     * @param zones Die zu analysierenden Zonen.
     * @return Gibt die gefundene Zone oder null zurück, sollte keine Zone gefunden werden.
     * @param <E> Der Zone class-type.
     */
    public static <E extends Zone> E findZoneNorthOf(E currentZone, int relativeX, E[] zones) {
        final int absoluteX = currentZone.absoluteXStart + relativeX;
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

    /**
     * Findet eine Zone, welche Südlich der übergebenen Zone liegt.
     * @param currentZone Die ausgangs Zone.
     * @param relativeX Die Position auf der x-Achse. Die Südlich liegende Zone muss die x-Achse an dieser Koordinate schneiden.
     * @param zones Die zu analysierenden Zonen.
     * @return Gibt die gefundene Zone oder null zurück, sollte keine Zone gefunden werden.
     * @param <E> Der Zone class-type.
     */
    public static <E extends Zone> E findZoneSouthOf(E currentZone, int relativeX, E[] zones) {
        final int absoluteX = currentZone.absoluteXStart + relativeX;
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

    /**
     * Findet eine Zone, welche Östlich der übergebenen Zone liegt.
     * @param currentZone Die ausgangs Zone.
     * @param relativeY Die Position auf der x-Achse. Die Östlich liegende Zone muss die x-Achse an dieser Koordinate schneiden.
     * @param zones Die zu analysierenden Zonen.
     * @return Gibt die gefundene Zone oder null zurück, sollte keine Zone gefunden werden.
     * @param <E> Der Zone class-type.
     */
    public static <E extends Zone> E findZoneEastOf(E currentZone, int relativeY, E[] zones) {
        final int absoluteY = currentZone.absoluteYStart + relativeY;
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

    /**
     * Findet eine Zone, welche Westlich der übergebenen Zone liegt.
     * @param currentZone Die ausgangs Zone.
     * @param relativeY Die Position auf der x-Achse. Die Westlich liegende Zone muss die x-Achse an dieser Koordinate schneiden.
     * @param zones Die zu analysierenden Zonen.
     * @return Gibt die gefundene Zone oder null zurück, sollte keine Zone gefunden werden.
     * @param <E> Der Zone class-type.
     */
    public static <E extends Zone> E findZoneWestOf(E currentZone, int relativeY, E[] zones) {
        final int absoluteY = currentZone.absoluteYStart + relativeY;
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
