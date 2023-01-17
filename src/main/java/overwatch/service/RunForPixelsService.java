package overwatch.service;

import overwatch.model.ProcessableZone;

public class RunForPixelsService {
    private static int INTERSECTION_THRESHOLD = 2;

    public static int[] walk(ProcessableZone currentZone, ProcessableZone[] zones, int x, int y, final byte direction){
        return new int[2];
    }

    public static int walkRight(final int absoluteY, int absoluteX, final ProcessableZone[] zones, final ProcessableZone zone, ProcessableZone zoneShortcut){
        XLoop: while (true){
            // Bounds check
            if(absoluteX >= (zone.capture.width - 1))
                return absoluteX;
            // Rechts wandern
            absoluteX++;
            // Pixel-Zustand auslesen
            ZoneService.PixelState pixelState = ZoneService.calculatePixelState(absoluteX, absoluteY, zones, zoneShortcut);
            if(pixelState.isZoneChanged)
                zoneShortcut = ZoneService.findZoneForPixel(absoluteX, absoluteY, zones);

            if(pixelState.isModified) {
                continue XLoop;
            }
            else {
                // Prüfe, ob angrenzende Pixel ebenfalls nicht belegt sind.
                for (int offsetX = 1; offsetX <= INTERSECTION_THRESHOLD && (offsetX + absoluteX) < (zone.capture.width-1); offsetX++){
                    pixelState = ZoneService.calculatePixelState(absoluteX + offsetX, absoluteY, zones, zoneShortcut);
                    if (pixelState.isModified)
                        continue XLoop;
                }
            }
            return absoluteX - 1;
        }
    }

    public static int[] findOuterBounds(final int startX, final int startY, final ProcessableZone zone, final ProcessableZone[] zones){
        int absoluteMinX = startX + zone.absoluteXStart;
        int absoluteMinY = startY + zone.absoluteYStart;
        int absoluteMaxX = absoluteMinX;
        int absoluteMaxY = absoluteMinY;
        
        int absoluteWalkerX = absoluteMinX;
        int absoluteWalkerY = absoluteMinY;

        ProcessableZone zoneShortcut = zone;
        // Bottom direction scan-line
        while (true) {
            // Bounds check
            if(absoluteWalkerY >= zone.capture.height)
                break;
            final int absoluteWalkerXStart = absoluteWalkerX;
            absoluteWalkerY++;

        }
        return new int[12];
    }

}
