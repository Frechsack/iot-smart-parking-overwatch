package overwatch.service;

import org.jetbrains.annotations.Nullable;
import overwatch.model.Capture;
import overwatch.model.ProcessableZone;

/**
 * Enthält statische Methoden für die Objekterkennung.
 */
public class ObjectAnalyserService {

    /**
     * Konstante, welche angibt wie viele Pixel beim durchlauf nach links und rechts ignoriert werden, sollten sie nicht mutiert sein.
     */
    private static final int INTERSECTION_THRESHOLD = 2;

    /**
     * Läuft entlang der x-Achse nach rechts, solange bis auf eine Reihe von nicht mutierten Pixeln gestoßen wird.
     * @param absoluteY Der Startpunkt auf der y-Achse in Absoluten Koordinaten.
     * @param absoluteX Der Startpunkt auf der x-Achse in Absoluten Koordinaten.
     * @param zones Die zu analysierenden Zonen.
     * @param capture Das Bildgerät um die Bildgröße zu berechnen.
     * @param zoneShortcut Optionaler Parameter für die optimistische Berechnung der aktuellen Zone.
     * @return Gibt die größte mögliche Absolute Koordinate auf der x-Achse zurück, die gefunden wurde.
     */
    public static int walkRight(final int absoluteY, int absoluteX, final ProcessableZone[] zones, final Capture capture,@Nullable ProcessableZone zoneShortcut){
        XLoop: while (true){
            // Bounds check
            if(absoluteX >= (capture.width - 1))
                return absoluteX;
            // Rechts wandern
            absoluteX++;
            // Pixel-Zustand auslesen
            ZoneService.PixelState pixelState = ZoneService.calculatePixelState(absoluteX, absoluteY, zones, zoneShortcut);

            if(pixelState.isZoneChanged)
                zoneShortcut = ZoneService.findZoneForPixel(absoluteX, absoluteY, zones);

            if(pixelState.isModified && pixelState.isExisting) {
                continue;
            }
            else {
                // Prüfe, ob angrenzende Pixel ebenfalls nicht belegt sind.
                for (int offsetX = 1; offsetX <= INTERSECTION_THRESHOLD && (offsetX + absoluteX) < (capture.width-1); offsetX++){
                    pixelState = ZoneService.calculatePixelState(absoluteX + offsetX, absoluteY, zones, zoneShortcut);
                    if(!pixelState.isExisting)
                        break;
                    if (pixelState.isModified)
                        continue XLoop;
                }
            }
            return absoluteX - 1;
        }
    }
    /**
     * Läuft entlang der x-Achse nach links, solange bis auf eine Reihe von nicht mutierten Pixeln gestoßen wird.
     * @param absoluteY Der Startpunkt auf der y-Achse in Absoluten Koordinaten.
     * @param absoluteX Der Startpunkt auf der x-Achse in Absoluten Koordinaten.
     * @param zones Die zu analysierenden Zonen.
     * @param zoneShortcut Optionaler Parameter für die optimistische Berechnung der aktuellen Zone.
     * @return Gibt die kleinste mögliche Absolute Koordinate auf der x-Achse zurück, die gefunden wurde.
     */
    public static int walkLeft(final int absoluteY, int absoluteX, final ProcessableZone[] zones, ProcessableZone zoneShortcut){
        XLoop: while(true) {
            if(absoluteX <= 0)
                return absoluteX;
            absoluteX--;
            ZoneService.PixelState pixelState = ZoneService.calculatePixelState(absoluteX, absoluteY, zones, zoneShortcut);
            if(pixelState.isZoneChanged)
                zoneShortcut = ZoneService.findZoneForPixel(absoluteX, absoluteY, zones);
            if(pixelState.isModified && pixelState.isExisting){
                continue;
            }
            else {
                for(int offsetX = 1; offsetX <= INTERSECTION_THRESHOLD && (absoluteX - offsetX) >= 0; offsetX++ ) {
                    pixelState = ZoneService.calculatePixelState(absoluteX-offsetX, absoluteY, zones, zoneShortcut);
                    if(!pixelState.isExisting)
                        break;
                    if(pixelState.isModified)
                        continue XLoop;
                }
            }
            return absoluteX+1;
        }
    }

    /**
     * Läuft entlang der y-Achse nach unten (inkrementieren der y-Koordinate).
     * Dabei werden die Extrema auf den gekreuzten x-Achsen ermittelt per {@link #walkLeft(int, int, ProcessableZone[], ProcessableZone)} und {@link #walkRight(int, int, ProcessableZone[], Capture, ProcessableZone)}.
     * @param absoluteX Der Startpunkt auf der x-Achse in Absoluten Koordinaten.
     * @param absoluteY Der Startpunkt auf der y-Achse in Absoluten Koordinaten.
     * @param capture Das Videogerät für die Berechnung der Bildgröße.
     * @param zones Die zu analysierenden Zonen.
     * @return Gibt ein Array mit den kleinsten und größten gefundenen Absoluten Koordinaten zurück in dem Format: [absoluteMinX, absoluteMinY, absoluteMaxX, absoluteMaxY].
     */
    private static int[] walkDown(int absoluteX, int absoluteY, final Capture capture, final ProcessableZone[] zones){
        return walkY(absoluteX, absoluteY, capture, zones, false);
    }

    /**
     * Läuft entlang der y-Achse nach oben (dekrementieren der y-Koordinate).
     * Dabei werden die Extrema auf den gekreuzten x-Achsen ermittelt per {@link #walkLeft(int, int, ProcessableZone[], ProcessableZone)} und {@link #walkRight(int, int, ProcessableZone[], Capture, ProcessableZone)}.
     * @param absoluteX Der Startpunkt auf der x-Achse in Absoluten Koordinaten.
     * @param absoluteY Der Startpunkt auf der y-Achse in Absoluten Koordinaten.
     * @param capture Das Videogerät für die Berechnung der Bildgröße.
     * @param zones Die zu analysierenden Zonen.
     * @return Gibt ein Array mit den kleinsten und größten gefundenen Absoluten Koordinaten zurück in dem Format: [absoluteMinX, absoluteMinY, absoluteMaxX, absoluteMaxY].
     */
    private static int[] walkUp(int absoluteX, int absoluteY, final Capture capture, final ProcessableZone[] zones){
        return walkY(absoluteX, absoluteY, capture, zones, true);
    }

    /**
     * Wandert von einer übergebenen Position aus über ein Objekt entlang der y-Achse. Dabei wird versucht ein maximal großes Rechteck zu finden.
     * @param absoluteX Der Startpunkt der Erkennung in absoluten Pixeln.
     * @param absoluteY Der Startpunkt der Erkennung in absoluten Pixeln.
     * @param capture Das Videogerät, welches für die Erkennung der Bildgrüße verwendet werden soll.
     * @param zones Die zu bearbeitenden Zonen.
     * @param isWalkUp Gibt an auf der y-Achse nach oben (dekrementierend) oder nach unten (inkrementierend) gewandert werden soll.
     * @return Gibt ein array mit vier Elementen zurück in dem Format: [absoluteMinX, absoluteMinY, absoluteMaxX, absoluteMaxY].
     */
    private static int[] walkY(int absoluteX, int absoluteY, final Capture capture, final ProcessableZone[] zones, final boolean isWalkUp){
        int absoluteMinX = absoluteX;
        int absoluteMinY = absoluteY;
        int absoluteMaxX = absoluteMinX;
        int absoluteMaxY = absoluteMinY;

        ProcessableZone zoneShortcut = null;
        YLoop: while (true) {

            // Bounds check
            if (isWalkUp){
                if(absoluteY <= 0) break;
            }
            else {
                if(absoluteY >= capture.height-2) break;
            }

            // Cursor Hoch/Runter bewegen
            if(isWalkUp) absoluteY--;
            else absoluteY++;

            ZoneService.PixelState pixelState = ZoneService.calculatePixelState(absoluteX, absoluteY, zones, zoneShortcut);

            if(pixelState.isZoneChanged)
                zoneShortcut = ZoneService.findZoneForPixel(absoluteX, absoluteY, zones);

            // Annahme: Rechteckige Zonen, es kann kein Pixel parallel auf der y-Achse bei anderem x-Wert existieren
            if(!pixelState.isExisting){
                if(isWalkUp) absoluteMinY = absoluteY;
                else absoluteMaxY = absoluteY;
                break;
            }

            if(pixelState.isModified){
                int x = walkLeft(absoluteY, absoluteX, zones, zoneShortcut);
                if(x < absoluteMinX)
                    absoluteMinX = x;
                x = walkRight(absoluteY, absoluteX, zones, capture, zoneShortcut);
                if(x > absoluteMaxX)
                    absoluteMaxX = x;
            }
            else {
                if(isWalkUp) absoluteY--;
                else absoluteY++;

                int unmodifiedPixelCounter = 0;
                final int absoluteCheckY = isWalkUp ? absoluteY -1 : absoluteY + 1;
                for(int absoluteCheckX = absoluteX; absoluteCheckX >= 0; absoluteCheckX--){
                    pixelState = ZoneService.calculatePixelState(absoluteCheckX, absoluteCheckY, zones, zoneShortcut);
                    if(pixelState.isModified){
                        absoluteX = absoluteCheckX;
                        continue YLoop;
                    }
                    else {
                        if(unmodifiedPixelCounter > INTERSECTION_THRESHOLD)
                            break;
                        unmodifiedPixelCounter++;
                    }
                }
                unmodifiedPixelCounter = 0;
                for(int absoluteCheckX = absoluteX; absoluteCheckX < capture.width; absoluteCheckX++) {
                    pixelState = ZoneService.calculatePixelState(absoluteCheckX, absoluteCheckY, zones, zoneShortcut);
                    if (pixelState.isModified) {
                        absoluteX = absoluteCheckX;
                        continue YLoop;
                    }
                    else {
                        if(unmodifiedPixelCounter > INTERSECTION_THRESHOLD)
                            break;
                        unmodifiedPixelCounter++;
                    }
                }

                if(isWalkUp) absoluteMinY = absoluteY;
                else absoluteMaxY = absoluteY;
                break;
            }
        }
        return new int[]{ absoluteMinX, absoluteMinY, absoluteMaxX, absoluteMaxY};
    }

    /**
     * Findet für ein Objekt an einer Position die Dimensionen und die Position.
     * @param relativeX Der Startpunkt der Erkennung auf der x-Achse in abhängigkeit zu der übergebenen Zone.
     * @param relativeY Der Startpunkt der Erkennung auf der x-Achse in abhängigkeit zu der übergebenen Zone.
     * @param zone Die Zone in deren Abhängigkeit die Koordinaten angegeben sind.
     *             Es wird außerdem von dieser Zone das Videogerät verwendet für die Erkennung der Bildgröße.
     * @param zones Die zu bearbeitenden Zonen.
     * @return Gibt ein array mit vier Elementen zurück in dem Format: [absoluteMinX, absoluteMinY, absoluteMaxX, absoluteMaxY].
     */
    public static int[] findOuterBounds(final int relativeX, final int relativeY, final ProcessableZone zone, final ProcessableZone[] zones){
        int absoluteX = relativeX + zone.absoluteXStart;
        int absoluteY = relativeY + zone.absoluteYStart;

        int[] walkDown = walkDown(absoluteX, absoluteY, zone.capture, zones);
        int[] walkUp = walkUp(absoluteX, absoluteY, zone.capture, zones);

        return new int[]{
                Math.min(walkDown[0], walkUp[0]),
                Math.min(walkDown[1], walkUp[1]),
                Math.max(walkDown[2], walkUp[2]),
                Math.max(walkDown[3], walkUp[3])
        };

    }

}
