package overwatch.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import overwatch.model.ProcessableZone;
import overwatch.model.Zone;

import java.util.Arrays;
import java.util.OptionalInt;

/**
 * Statische Methoden für die Berechnung von Zonen übergreifenden Operationen.
 */
public class ZoneService {

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
     * Prüft, ob ein Pixel mutiert ist.
     * @param x Die absolute Position des Pixels auf der x-Achse.
     * @param y Die absolute Position des Pixels auf der y-Achse.
     * @param zones Die auszuwertenden Zonen.
     * @param shortcut Eine optionale Zone, die zuerst durchsucht werden soll.
     * @return Gibt zurück, ob der Pixel mutiert wurde oder nicht.
     */
    public static @NotNull PixelState calculatePixelState(final int x, final int y, final ProcessableZone[] zones, @Nullable final ProcessableZone shortcut) {
        if (shortcut != null && shortcut.x() <= x && shortcut.endX() >= x && shortcut.y() <= y && shortcut.endY() >= y)
            return shortcut.isModified(x - shortcut.x(), y - shortcut.y())
                    ? PixelState.MODIFIED
                    : PixelState.UNMODIFIED;

        for (ProcessableZone zone : zones) {
            if (zone.x() <= x && zone.endX() >= x && zone.y() <= y && zone.endY() >= y)
                return zone.isModified(x - zone.x(), y - zone.y())
                        ? PixelState.MODIFIED_ZONE_CHANGE
                        : PixelState.UNMODIFIED_ZONE_CHANGE;
        }
        return PixelState.NOT_EXISTING;
    }
}
