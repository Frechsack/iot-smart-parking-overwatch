package overwatch.service;

import org.jetbrains.annotations.Nullable;
import overwatch.skeleton.Outline;
import overwatch.model.*;
import overwatch.skeleton.Rectangle;
import overwatch.skeleton.Size;

import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Enthält statische Methoden für die Objekterkennung.
 */
public class ObjectAnalyserService {

    /**
     * Konstante, welche angibt wie viele Pixel beim durchlauf nach links und rechts ignoriert werden, sollten sie nicht mutiert sein.
     */
    private static final int INTERSECTION_THRESHOLD = 2;

    private ObjectAnalyserService() {}

    public static int walkRight(final int startX, final int y, final ProcessableZone[] zones, final Size size, @Nullable ProcessableZone zoneShortcut){
        return IntStream.range(startX, size.width())
                .parallel()
                .filter(x -> {
                    ZoneService.PixelState pixelState = ZoneService.calculatePixelState(x, y, zones, zoneShortcut);
                    return !pixelState.isModified && pixelState.isExisting;
                })
                .filter(x-> IntStream.range(x+1,Math.min(x+INTERSECTION_THRESHOLD, size.width()))
                        .filter(walkerX->ZoneService.calculatePixelState(walkerX, y, zones, zoneShortcut).isModified)
                        .findAny().isEmpty())
                .findFirst().orElse(y);
    }

    public static int walkLeft(final int startX, final int y, final ProcessableZone[] zones, @Nullable ProcessableZone zoneShortcut){
        return IntStream.iterate(startX, x-> x>0, x-> x-1)
                .parallel()
                .filter(x -> {
                    ZoneService.PixelState pixelState = ZoneService.calculatePixelState(x, y, zones, zoneShortcut);
                    return !pixelState.isModified && pixelState.isExisting;
                })
                .filter(x-> IntStream.range(x-1,Math.max(x-INTERSECTION_THRESHOLD, 0))
                        .filter(walkerX->ZoneService.calculatePixelState(walkerX, y, zones, zoneShortcut).isModified)
                        .findAny().isEmpty())
                .findFirst().orElse(y);
    }

    private static Outline walkDown(int x, int y, final Size size, final ProcessableZone[] zones, @Nullable ProcessableZone shortcut){
        int minX = x, lastMinX = x, maxX = x, lastMaxX = x, minY = y, maxY = y;

        for (; y < size.height(); y++){
            ZoneService.PixelState pixelState = ZoneService.calculatePixelState(x, y, zones, shortcut);

            if (pixelState.isZoneChanged)
                shortcut = Outline.findOutlineForPosition(x, y, zones);
            if(!pixelState.isExisting)
                break;
            else if(!pixelState.isModified){
                // Korrektur X
                final int finalX = x;
                final int checkY = y;
                final @Nullable ProcessableZone finalShortcut = shortcut;
                OptionalInt possibleX = IntStream.rangeClosed(lastMinX, lastMaxX)
                        .parallel()
                        .filter(checkX -> finalX != checkX)
                        .filter(checkX -> ZoneService.calculatePixelState(checkX, checkY, zones, finalShortcut).isModified)
                        .findAny();
                if(possibleX.isPresent())
                    x = possibleX.getAsInt();
                else
                    break;
            }
            lastMinX = walkLeft(x, y, zones, shortcut);
            lastMaxX =  walkRight(x, y , zones, size, shortcut);
            minX = Math.min(lastMinX, minX);
            maxX = Math.max(lastMaxX, maxX);
            maxY = y;

        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }



    private static Outline walkUp(int x, int y, final Size size, final ProcessableZone[] zones, @Nullable ProcessableZone shortcut){
        int minX = x, lastMinX = x, maxX = x, lastMaxX =x, minY = y, maxY = y;

        for (; y > 0; y--){
            ZoneService.PixelState pixelState = ZoneService.calculatePixelState(x, y, zones, shortcut);

            if (pixelState.isZoneChanged)
                shortcut = Outline.findOutlineForPosition(x, y, zones);

            if(!pixelState.isExisting)
                break;
            else if(!pixelState.isModified){
                // Korrektur X
                final int finalX = x;
                final int checkY = y;
                final @Nullable ProcessableZone finalShortcut = shortcut;
                OptionalInt possibleX = IntStream.rangeClosed(lastMinX, lastMaxX)
                        .parallel()
                        .filter(checkX -> finalX != checkX)
                        .filter(checkX -> ZoneService.calculatePixelState(checkX, checkY, zones, finalShortcut).isModified)
                        .findAny();
                if(possibleX.isPresent())
                    x = possibleX.getAsInt();
                else
                    break;
            }
            lastMinX = walkLeft(x, y, zones, shortcut);
            lastMaxX =  walkRight(x, y, zones, size, shortcut);
            minX = Math.min(lastMinX, minX);
            maxX = Math.max(lastMaxX, maxX);
            minY = y;

        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public static Outline findObjectBounds(final int x, final int y, final Size size, final ProcessableZone[] zones, @Nullable final ProcessableZone shortcut){
        final Outline upperOutline = walkDown(x, y, size, zones, shortcut);
        final Outline lowerOutline = walkUp(x, y, size, zones, shortcut);
        return Outline.compose(upperOutline, lowerOutline);

    }

}
