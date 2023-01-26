package overwatch.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import overwatch.model.ProcessableZone;
import overwatch.skeleton.Outline;
import overwatch.skeleton.Size;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static overwatch.skeleton.Outline.isIntersecting;
import static overwatch.skeleton.Outline.of;

/**
 * Enthält statische Methoden für die Objekterkennung.
 */
public class ObjectAnalyserService {

    /**
     * Konstante, welche angibt wie viele Pixel beim durchlauf nach links und rechts ignoriert werden, sollten sie nicht mutiert sein.
     */
    private static final int INTERSECTION_THRESHOLD = 5;

    private ObjectAnalyserService() {}

    private static final int SKIP_PIXELS = 15;
    private static final int SIGNIFICANT_AREA_TO_DETECT = 45;


    public static <E extends Outline> Collection<E> findZonesWithObject(final E[] zones, Collection<Outline> objects){
        return objects.parallelStream()
                .map(object -> {
                    E maxZone = null;
                    int maxOverlap = 0;
                    for (E zone : zones){
                        if(isIntersecting(zone, object)) {
                            int overlap = Outline.intersectionArea(zone, object);
                            if (overlap > maxOverlap){
                                maxOverlap = overlap;
                                maxZone = zone;
                            }
                        }
                    }
                    return maxZone;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Findet alle Objekte innerhalb der übergebenen Zonen.
     * @param zones Die zu prüfenden Zonen.
     * @return Gibt eine Liste mit allen gefundenen Objekten zurück.
     */
    public static List<Outline> findObjects(final ProcessableZone[] zones){
        final Outline outerBounds = Outline.compose(zones);
        final List<Outline> bounds = new ArrayList<>(Arrays.stream(zones)
                .parallel()
                .flatMap(zone -> IntStream.iterate(zone.x(), x -> x + SKIP_PIXELS <= zone.endX(), x -> x + SKIP_PIXELS)
                        .boxed()
                        .parallel()
                        .flatMap(x -> IntStream.iterate(zone.y(), y -> y + SKIP_PIXELS <= zone.endY(), y -> y + SKIP_PIXELS)
                                .filter(y -> ZoneService.calculatePixelState(x, y, zones, zone).isModified)
                                .mapToObj(y -> ObjectAnalyserService.findObjectBounds(x, y, outerBounds, zones, zone))
                                .filter(outline -> outline.area() >= SIGNIFICANT_AREA_TO_DETECT)
                        ))
                .toList());
        for (int i = 0; i < bounds.size(); i++){
            Outline a = bounds.get(i);
            for (int y = i; y < bounds.size(); y++) {
                Outline b = bounds.get(y);
                if(a == b)
                    continue;
                if(!isIntersecting(a, b))
                    continue;
                a = Outline.compose(a, b);
                bounds.remove(y--);
            }
            bounds.set(i, a);
        }
        return bounds;
    }

    @Deprecated
    public static List<Outline> findObjectsSequential(final ProcessableZone[] zones){
        final List<Outline> objects = new ArrayList<>(Arrays.stream(zones)
                .parallel()
                .flatMap(zone -> {
                    final List<Outline> outlineList = new ArrayList<>();
                    for(int x = zone.x(); x < zone.endX() -1; x += SKIP_PIXELS ) {
                        for(int y = zone.y(); y < zone.endY() -1; y += SKIP_PIXELS ) {
                            ZoneService.PixelState pixelState = ZoneService.calculatePixelState(x, y, zones, zone);
                            if(!pixelState.isModified) continue;
                            Outline outline = ObjectAnalyserService.findObjectBounds(x, y, zone.capture(), zones, zone);
                            if(outline.area() >= SIGNIFICANT_AREA_TO_DETECT)
                                outlineList.add(outline);
                        }
                    }
                    return outlineList.stream();
                }).toList());
        final List<Outline> composedBounds = new ArrayList<>(objects.size());

        while (!objects.isEmpty()){
            Outline a = objects.remove(0);
            for (int i = 0; i < objects.size(); i++) {
                Outline b = objects.get(i);
                if(!isIntersecting(a, b))
                    continue;
                a = Outline.compose(a, b);
                objects.remove(i--);
            }
            composedBounds.add(a);
        }
        return composedBounds;
    }

    private static int walkRight(final int startX, final int y, final ProcessableZone[] zones, final Outline outerBounds, @Nullable ProcessableZone zoneShortcut){
        return IntStream.rangeClosed(startX, outerBounds.endX())
                .filter(x -> {
                    ZoneService.PixelState pixelState = ZoneService.calculatePixelState(x, y, zones, zoneShortcut);
                    return !pixelState.isModified && pixelState.isExisting;
                })
                .filter(x-> IntStream.rangeClosed(x+1,Math.min(x+INTERSECTION_THRESHOLD, outerBounds.endX()))
                        .parallel()
                        .noneMatch(walkerX->ZoneService.calculatePixelState(walkerX, y, zones, zoneShortcut).isModified)
                )
                .findFirst().orElse(startX);
    }

    private static int walkLeft(final int startX, final int y, final ProcessableZone[] zones, final Outline outerBounds, @Nullable ProcessableZone zoneShortcut){
        return IntStream.iterate(startX, x-> x>=outerBounds.x(), x-> x-1)
                .filter(x -> {
                    ZoneService.PixelState pixelState = ZoneService.calculatePixelState(x, y, zones, zoneShortcut);
                    return !pixelState.isModified && pixelState.isExisting;
                })
                .filter(x-> IntStream.iterate(x-1, checkX -> checkX >= Math.max(x-INTERSECTION_THRESHOLD, outerBounds.x()), checkX -> checkX-1)
                        .parallel()
                        .noneMatch(walkerX->ZoneService.calculatePixelState(walkerX, y, zones, zoneShortcut).isModified)
                )
                .findFirst().orElse(startX);
    }

    private static @NotNull Outline walkDown(int x, int y, final Outline outerBounds, final ProcessableZone[] zones, @Nullable ProcessableZone shortcut){
        int minX = x, lastMinX = x, maxX = x, lastMaxX = x, minY = y, maxY = y;

        for (; y <= outerBounds.endY(); y++){
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
            lastMinX = walkLeft(x, y, zones, outerBounds, shortcut);
            lastMaxX =  walkRight(x, y , zones, outerBounds, shortcut);
            minX = Math.min(lastMinX, minX);
            maxX = Math.max(lastMaxX, maxX);
            maxY = y;

        }
        return Outline.of(minX, minY, maxX - minX, maxY - minY);
    }

    private static @NotNull Outline walkUp(int x, int y, final Outline outerBounds, final ProcessableZone[] zones, @Nullable ProcessableZone shortcut){
        int minX = x, lastMinX = x, maxX = x, lastMaxX =x, minY = y, maxY = y;

        for (; y >= outerBounds.y(); y--){
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
            lastMinX = walkLeft(x, y, zones, outerBounds, shortcut);
            lastMaxX =  walkRight(x, y, zones, outerBounds, shortcut);
            minX = Math.min(lastMinX, minX);
            maxX = Math.max(lastMaxX, maxX);
            minY = y;

        }
        return of(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Findet zu einer übergebenen Position ein Objekt. Es wird angenommen das an der Position ein Objekt ist.
     * Die ermittelte Fläche sollten anhand ihrer Größe {@link Outline#area()} überprüft werden.
     * @param x Position auf der x-Achse, auf der die Suche beginnt. Dieser Punkt muss innerhalb eines Objekts liegen.
     * @param y Position auf der y-Achse, auf der die Suche beginnt. Dieser Punkt muss innerhalb eines Objekts liegen.
     * @param outerBounds Die zu durchsuchende Fläche.
     * @param zones Die zu prüfenden Zonen.
     * @param shortcut Optionale Shortcut für eine zonenschätzung.
     * @return Gibt die gefundene Fläche zurück.
     */
    public static @NotNull Outline findObjectBounds(final int x, final int y, final Outline outerBounds, final ProcessableZone[] zones, @Nullable final ProcessableZone shortcut){
        final Outline upperOutline = walkDown(x, y, outerBounds, zones, shortcut);
        final Outline lowerOutline = walkUp(x, y, outerBounds, zones, shortcut);
        return Outline.compose(upperOutline, lowerOutline);
    }

}
