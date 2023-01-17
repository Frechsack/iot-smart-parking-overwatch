package overwatch.service;

import overwatch.model.ProcessableZone;
import overwatch.model.Zone;

public class RunForPixelsService {
    private static final int INTERSECTION_THRESHOLD = 2;

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

    public static int walkLeft(final int absoluteY, int absoluteX, final ProcessableZone[] zones, final ProcessableZone zone, ProcessableZone zoneShortcut){
        XLoop: while(true) {
            if(absoluteX <= 0)
                return absoluteX;
            absoluteX--;
            ZoneService.PixelState pixelState = ZoneService.calculatePixelState(absoluteX, absoluteY, zones, zoneShortcut);
            if(pixelState.isZoneChanged)
                zoneShortcut = ZoneService.findZoneForPixel(absoluteX, absoluteY, zones);
            if(pixelState.isModified){
                continue XLoop;
            }
            else {
                for(int offsetX = 1; offsetX <= INTERSECTION_THRESHOLD && (absoluteX - offsetX) >= 0; offsetX++ ) {
                    pixelState = ZoneService.calculatePixelState(absoluteX-offsetX, absoluteY, zones, zoneShortcut);
                    if(pixelState.isModified)
                        continue XLoop;
                }
            }
            return absoluteX+1;
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
        YLoop: while (true) {
            // Bounds check
            if(absoluteWalkerY >= zone.capture.height-2)
                break;

            absoluteWalkerY++;

            ZoneService.PixelState pixelState = ZoneService.calculatePixelState(absoluteWalkerX, absoluteWalkerY, zones, zoneShortcut);
            if(pixelState.isModified){
                int x = walkLeft(absoluteWalkerY, absoluteWalkerX, zones, zone, zoneShortcut);
                if(x < absoluteMinX){
                    absoluteMinX = x;

                }
                x = walkRight(absoluteWalkerY, absoluteWalkerX, zones,zone, zoneShortcut);
                if(x > absoluteMaxX){
                    absoluteMaxX = x;
                }



            } else {

                absoluteWalkerY--;
                for(int absoluteCheckX = absoluteWalkerX; absoluteCheckX >= 0; absoluteCheckX--){
                    pixelState = ZoneService.calculatePixelState(absoluteCheckX, absoluteWalkerY+1, zones, zoneShortcut);
                    if(pixelState.isModified){
                        absoluteWalkerX = absoluteCheckX;
                        continue YLoop;
                    }
                }

                for(int absoluteCheckX = absoluteWalkerX; absoluteCheckX < zone.capture.width; absoluteCheckX++) {
                    pixelState = ZoneService.calculatePixelState(absoluteCheckX, absoluteWalkerY + 1, zones, zoneShortcut);
                    if (pixelState.isModified) {
                        absoluteWalkerX = absoluteCheckX;
                        continue YLoop;
                    }
                }
                absoluteMaxY = absoluteWalkerY;
                break;

            }

        }
        return new int[]{absoluteMinX, absoluteMinY, absoluteMaxX, absoluteMaxY};
    }

}
