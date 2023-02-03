package overwatch.algorithm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import overwatch.algorithm.dongle.DongleAlgorithm;
import overwatch.model.Zone;
import overwatch.service.ConfigurationService;
import overwatch.skeleton.Outline;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static overwatch.skeleton.Outline.isIntersecting;

/**
 * Basisklasse für Bewegungserkennung.
 */
public abstract class Algorithm {

    /**
     * Mindestgröße von Objekten damit sie erkannt werden.
     */
    protected static final int SIGNIFICANT_AREA_TO_DETECT = 60;

    /**
     * Grenze, damit aneinanderliegende Objekte miteinander kombiniert werden.
     */
    protected static final int INTERSECTION_THRESHOLD = 40;

    /**
     * Farbe von nicht modifizierten Pixeln.
     */
    protected static final Color UNMODIFIED_PIXEL_COLOR = Color.white;

    /**
     * Farbe von modifizierten Pixeln.
     */
    protected static final Color MODIFIED_PIXEL_COLOR = Color.black;

    /**
     * Farbe für Schrift und Zonenbegrenzungen.
     */
    protected static final Color ZONE_BOUNDS_COLOR = Color.blue;

    /**
     * Farbe für Objektumrandungen.
     */
    protected static final Color OBJECT_OUTLINE_COLOR = Color.red;

    /**
     * Farbe für den Hintergrund von aktiven Zonen.
     */
    protected static final Color ACTIVE_ZONE_COLOR = Color.green;

    /**
     * Erstellt eine neue Instanz des Algorithmus.
     * @param zones Die auszuwertenden Zonen.
     * @return Gibt eine neue Instanz von {@link DongleAlgorithm} oder {@link OpenCvAlgorithm} zurück.
     */
    public static Algorithm create(@NotNull Zone[] zones){
        boolean isOpenCvEnabled = ConfigurationService.getBoolean(ConfigurationService.Keys.ANALYSE_OPENCV_ENABLE);
        return isOpenCvEnabled
                ? new OpenCvAlgorithm(zones)
                : new DongleAlgorithm(zones);
    }

    /**
     * Stoppt den Algorithmus und gibt alle Ressourcen frei.
     */
    public abstract void close();

    /**
     * Berechnet alle aktiven Zonen im aktuellen Frame und gibt diese zurück.
     * @return Eine Collection mit allen aktiven Zonen.
     */
    public abstract @NotNull @UnmodifiableView Collection<? extends Zone> compute();

    /**
     * Berechnet eine grafische Auswertung des aktuellen Frames.
     * @return Gibt die Grafische Auswertung zurück.
     */
    public abstract @NotNull BufferedImage computeImage();

    /**
     * Template um die grafische Ausgabe zu rendern.
     * @param image Die Bildgrundlage, auf die gerendert werden soll.
     * @param isPixelModified Predicate welches angibt, ob ein Pixel modifiziert ist.
     * @param zones Die ausgewerteten Zonen.
     * @param activeZones Die aktiven Zonen.
     * @param objects Alle Umrisse von erkannten Objekten.
     */
    protected static void renderImage(final @NotNull BufferedImage image, final @NotNull  BiPredicate<Integer, Integer> isPixelModified,  final @NotNull Zone[] zones, final @NotNull @UnmodifiableView Collection<? extends Zone> activeZones, final @NotNull @UnmodifiableView Collection<Outline> objects ){
        final Graphics graphics = image.getGraphics();
        graphics.setColor(UNMODIFIED_PIXEL_COLOR);
        graphics.fillRect(0,0,image.getWidth(), image.getHeight());

        graphics.setColor(ACTIVE_ZONE_COLOR);
        activeZones.forEach(it -> graphics.fillRect(it.x(), it.y(), it.width(), it.height()));

        graphics.setColor(MODIFIED_PIXEL_COLOR);
        objects.forEach(it -> IntStream.rangeClosed(it.x(), it.endX())
                .forEach(x ->
                        IntStream.rangeClosed(it.y(), it.endY())
                                .filter(y -> isPixelModified.test(x,y))
                                .forEach(y -> graphics.fillRect(x,y,1,1))
                ));

        graphics.setColor(OBJECT_OUTLINE_COLOR);
        objects.forEach(it -> graphics.drawRect(it.x(), it.y(), it.width(), it.height()));

        graphics.setColor(ZONE_BOUNDS_COLOR);
        Arrays.stream(zones).forEach(it -> {
            graphics.drawRect(it.x(), it.y(), it.width(), it.height());
            graphics.drawString(Integer.toString(it.nr()), it.x() + 5, it.y() + 10);
        });
        graphics.dispose();
    }

    /**
     * Findet alle aktiven Zonen. Eine Zone ist aktiv, wenn mindestens ein Objekt in ihr liegt.
     * @param zones Die zu prüfenden Zonen.
     * @param objects Die erkannten Objekte.
     * @return Gibt einen Stream mit aktiven Zonen zurück,.
     * @param <E> Der Typ Zone.
     */
    protected static @NotNull <E extends Outline> Stream<E> findActiveZones(final E[] zones, Collection<Outline> objects){
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
                .distinct();
    }
}
