package overwatch.algorithm.dongle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import overwatch.algorithm.Algorithm;
import overwatch.model.Capture;
import overwatch.model.Zone;
import overwatch.skeleton.Outline;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static overwatch.skeleton.Outline.isIntersecting;
import static overwatch.skeleton.Outline.of;

public final class DongleAlgorithm extends Algorithm {

    private static final int SKIP_PIXELS = 25;

    /**
     * Die Auszuwertenden Captures.
     */
    private final @NotNull Capture[] captures;

    /**
     * Die Auszuwertenden Zonen.
     */
    private final @NotNull DongleProcessableZone[] zones;

    /**
     * Grundlage für das zu rendernde Bild.
     */
    private final @NotNull BufferedImage image;

    /**
     * Der auszuwertende Rahmen.
     */
    private final @NotNull Outline outerBounds;

    /**
     * Collection mit allen erkannten Objekten im aktuellen Frame.
     */
    private volatile @NotNull @UnmodifiableView Collection<Outline> objects = List.of();

    /**
     * Collection mit allen aktiven Zonen. Eine Zone ist aktiv, wenn mindestens ein Objekt in ihr liegt.
     */
    private volatile @NotNull @UnmodifiableView Collection<? extends Zone> activeZones = List.of();

    public DongleAlgorithm(@NotNull Zone[] zones) {
        super();
        this.zones = Arrays.stream(zones)
                .map(DongleProcessableZone::new)
                .toArray(DongleProcessableZone[]::new);
        this.captures = Arrays.stream(zones).map(Zone::capture).distinct().toArray(Capture[]::new);
        this.outerBounds = Outline.compose(captures);
        this.image = new BufferedImage(outerBounds.width(), outerBounds.height(), BufferedImage.TYPE_INT_RGB);

        // ImageService sollte Quelldaten neu laden.
        Arrays.stream(captures).forEach(DongleImageService::updateSourceImage);
    }


    @Override
    public void close() {
        Arrays.stream(zones).parallel().forEach(DongleProcessableZone::reset);
        image.flush();
    }

    @Override
    public synchronized @NotNull @UnmodifiableView Collection<? extends Zone> compute() {
        Arrays.stream(zones).parallel().forEach(DongleProcessableZone::reset);
        updateObjects();
        updateActiveZones();
        return activeZones;
    }

    @Override
    public synchronized @NotNull BufferedImage computeImage() {
        final @Nullable @UnmodifiableView Collection<Outline> outlines = this.objects;
        final @Nullable @UnmodifiableView Collection<? extends Zone> zonesWithObjects = this.activeZones;
        final BiPredicate<Integer, Integer> isPixelModified = (x,y) -> calculatePixelState(x,y, null).isModified;

        Arrays.stream(captures).parallel().forEach(DongleImageService::updateCurrentImage);

        renderImage(image, isPixelModified, zones, zonesWithObjects, outlines);
        return image;
    }

    private int walkRight(final int startX, final int y, @Nullable DongleProcessableZone zoneShortcut){
        return IntStream.rangeClosed(startX, outerBounds.endX())
                .filter(x -> {
                    PixelState pixelState = calculatePixelState(x, y, zoneShortcut);
                    return !pixelState.isModified && pixelState.isExisting;
                })
                .filter(x-> IntStream.rangeClosed(x+1,Math.min(x+INTERSECTION_THRESHOLD, outerBounds.endX()))
                        .parallel()
                        .noneMatch(walkerX->calculatePixelState(walkerX, y, zoneShortcut).isModified)
                )
                .findFirst().orElse(startX);
    }

    private int walkLeft(final int startX, final int y, @Nullable DongleProcessableZone zoneShortcut){
        return IntStream.iterate(startX, x-> x>=outerBounds.x(), x-> x-1)
                .filter(x -> {
                    PixelState pixelState = calculatePixelState(x, y, zoneShortcut);
                    return !pixelState.isModified && pixelState.isExisting;
                })
                .filter(x-> IntStream.iterate(x-1, checkX -> checkX >= Math.max(x-INTERSECTION_THRESHOLD, outerBounds.x()), checkX -> checkX-1)
                        .parallel()
                        .noneMatch(walkerX->calculatePixelState(walkerX, y, zoneShortcut).isModified)
                )
                .findFirst().orElse(startX);
    }

    private @NotNull Outline walkDown(int x, int y, @Nullable DongleProcessableZone shortcut){
        int minX = x, lastMinX = x, maxX = x, lastMaxX = x, minY = y, maxY = y;

        for (; y <= outerBounds.endY(); y++){
            PixelState pixelState = calculatePixelState(x, y, shortcut);

            if (pixelState.isZoneChanged)
                shortcut = Outline.findOutlineForPosition(x, y, zones);
            if(!pixelState.isExisting)
                break;
            else if(!pixelState.isModified){
                // Korrektur X
                final int finalX = x;
                final int checkY = y;
                final @Nullable DongleProcessableZone finalShortcut = shortcut;
                OptionalInt possibleX = IntStream.rangeClosed(lastMinX, lastMaxX)
                        .parallel()
                        .filter(checkX -> finalX != checkX)
                        .filter(checkX -> calculatePixelState(checkX, checkY, finalShortcut).isModified)
                        .findAny();
                if(possibleX.isPresent())
                    x = possibleX.getAsInt();
                else
                    break;
            }
            lastMinX = walkLeft(x, y, shortcut);
            lastMaxX =  walkRight(x, y, shortcut);
            minX = Math.min(lastMinX, minX);
            maxX = Math.max(lastMaxX, maxX);
            maxY = y;

        }
        return Outline.of(minX, minY, maxX - minX, maxY - minY);
    }

    private @NotNull Outline walkUp(int x, int y, @Nullable DongleProcessableZone shortcut){
        int minX = x, lastMinX = x, maxX = x, lastMaxX =x, minY = y, maxY = y;

        for (; y >= outerBounds.y(); y--){
            PixelState pixelState = calculatePixelState(x, y, shortcut);

            if (pixelState.isZoneChanged)
                shortcut = Outline.findOutlineForPosition(x, y, zones);

            if(!pixelState.isExisting)
                break;
            else if(!pixelState.isModified){
                // Korrektur X
                final int finalX = x;
                final int checkY = y;
                final @Nullable DongleProcessableZone finalShortcut = shortcut;
                OptionalInt possibleX = IntStream.rangeClosed(lastMinX, lastMaxX)
                        .parallel()
                        .filter(checkX -> finalX != checkX)
                        .filter(checkX -> calculatePixelState(checkX, checkY, finalShortcut).isModified)
                        .findAny();
                if(possibleX.isPresent())
                    x = possibleX.getAsInt();
                else
                    break;
            }
            lastMinX = walkLeft(x, y, shortcut);
            lastMaxX =  walkRight(x, y, shortcut);
            minX = Math.min(lastMinX, minX);
            maxX = Math.max(lastMaxX, maxX);
            minY = y;

        }
        return of(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Findet ein Objekt an der angegebenen Position. Sollte an dieser Position kein Objekt liegen, wird ein Objekt mit der Größe 1x1 zurückgegeben.
     * @param x Die Position auf der x-Achse.
     * @param y Die Position auf der y-Achse.
     * @param shortcut Ein optionaler Parameter um schnellere Abfragen durchzuführen.
     * @return Gibt den Umriss eines Objekts zurück.
     */
    private @NotNull Outline findObjectBounds(final int x, final int y, @Nullable final DongleProcessableZone shortcut){
        final Outline upperOutline = walkDown(x, y, shortcut);
        final Outline lowerOutline = walkUp(x, y, shortcut);
        return Outline.compose(upperOutline, lowerOutline);
    }

    /**
     * Aktualisiert atomar alle aktiven Zonen und speichert diese in {@link #activeZones}.
     */
    private void updateActiveZones(){
        this.activeZones = findActiveZones(this.zones, this.objects).toList();
    }

    /**
     * Aktualisiert atomar alle Objekte und speichert diese in {@link #objects}.
     */
    private void updateObjects(){
        final List<Outline> objects = Arrays.stream(zones)
                .parallel()
                .flatMap(zone -> IntStream.iterate(zone.x(), x -> x + SKIP_PIXELS <= zone.endX(), x -> x + SKIP_PIXELS)
                        .boxed()
                        .parallel()
                        .flatMap(x -> IntStream.iterate(zone.y(), y -> y + SKIP_PIXELS <= zone.endY(), y -> y + SKIP_PIXELS)
                                .filter(y -> calculatePixelState(x, y, zone).isModified)
                                .mapToObj(y -> findObjectBounds(x, y, zone))
                                .filter(outline -> outline.area() >= SIGNIFICANT_AREA_TO_DETECT)
                        ))
                .collect(Collectors.toList());
        for (int i = 0; i < objects.size(); i++){
            Outline a = objects.get(i);
            for (int y = i; y < objects.size(); y++) {
                Outline b = objects.get(y);
                if(a == b)
                    continue;
                if(!isIntersecting(a, b, INTERSECTION_THRESHOLD))
                    continue;
                a = Outline.compose(a, b);
                objects.remove(y--);
            }
            objects.set(i, a);
        }
        this.objects = Collections.unmodifiableList(objects);
    }

    /**
     * Der Zustand eines Pixels.
     */
    private enum PixelState {

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


    /**
     * Prüft, ob ein Pixel mutiert ist.
     * @param x Die absolute Position des Pixels auf der x-Achse.
     * @param y Die absolute Position des Pixels auf der y-Achse.
     * @param shortcut Eine optionale Zone, die zuerst durchsucht werden soll.
     * @return Gibt zurück, ob der Pixel mutiert wurde oder nicht.
     */
    private @NotNull PixelState calculatePixelState(final int x, final int y, @Nullable final DongleProcessableZone shortcut) {
        if (shortcut != null && shortcut.x() <= x && shortcut.endX() >= x && shortcut.y() <= y && shortcut.endY() >= y)
            return shortcut.isModified(x - shortcut.x(), y - shortcut.y())
                    ? PixelState.MODIFIED
                    : PixelState.UNMODIFIED;

        for (DongleProcessableZone zone : zones) {
            if (zone.x() <= x && zone.endX() >= x && zone.y() <= y && zone.endY() >= y)
                return zone.isModified(x - zone.x(), y - zone.y())
                        ? PixelState.MODIFIED_ZONE_CHANGE
                        : PixelState.UNMODIFIED_ZONE_CHANGE;
        }
        return PixelState.NOT_EXISTING;
    }

}
