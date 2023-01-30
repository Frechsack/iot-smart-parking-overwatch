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

public abstract class Algorithm {

    protected static final int SKIP_PIXELS = 25;
    protected static final int SIGNIFICANT_AREA_TO_DETECT = 60;
    protected static final int INTERSECTION_THRESHOLD = 40;

    protected static final Color UNMODIFIED_PIXEL_RGB = Color.white;

    protected static final Color MODIFIED_PIXEL_RGB = Color.black;

    protected static final Color ZONE_BOUNDS_RGB = Color.blue;

    protected static final Color OUTLINES_RGB = Color.red;

    protected static final Color ZONE_TAKEN_RGB = Color.green;

    public static Algorithm create(@NotNull Zone[] zones){
        boolean isOpenCvEnabled = ConfigurationService.getBoolean(ConfigurationService.Keys.ANALYSE_OPENCV_ENABLE);
        return isOpenCvEnabled
                ? new OpenCvAlgorithm(zones)
                : new DongleAlgorithm(zones);
    }


    public abstract void close();
    
    public abstract @NotNull @UnmodifiableView Collection<? extends Zone> compute();
    
    public abstract BufferedImage computeImage();

    protected static void renderImage(final @NotNull BufferedImage image, final @NotNull  BiPredicate<Integer, Integer> isPixelModified,  final @NotNull Zone[] zones, final @NotNull Collection<? extends Zone> zonesWithObjects, final @NotNull Collection<Outline> objectOutlines ){
        final Graphics graphics = image.getGraphics();
        graphics.setColor(UNMODIFIED_PIXEL_RGB);
        graphics.fillRect(0,0,image.getWidth(), image.getHeight());

        graphics.setColor(ZONE_TAKEN_RGB);
        zonesWithObjects.forEach(it -> {
            graphics.fillRect(it.x(), it.y(), it.width(), it.height());
        });

        graphics.setColor(MODIFIED_PIXEL_RGB);
        objectOutlines.forEach(it -> {
            IntStream.rangeClosed(it.x(), it.endX())
                    .forEach(x ->
                            IntStream.rangeClosed(it.y(), it.endY())
                                    .filter(y -> isPixelModified.test(x,y))
                                    .forEach(y -> graphics.fillRect(x,y,1,1))
                    );
        });

        graphics.setColor(OUTLINES_RGB);
        objectOutlines.forEach(it -> graphics.drawRect(it.x(), it.y(), it.width(), it.height()));

        graphics.setColor(ZONE_BOUNDS_RGB);
        Arrays.stream(zones).forEach(it -> {
            graphics.drawRect(it.x(), it.y(), it.width(), it.height());
            graphics.drawString(Integer.toString(it.nr()), it.x() + 5, it.y() + 10);
        });


        graphics.dispose();
    }

    protected static <E extends Outline> Stream<E> findZonesWithObject(final E[] zones, Collection<Outline> objects){
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
