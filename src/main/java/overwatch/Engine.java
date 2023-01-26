package overwatch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import overwatch.model.Capture;
import overwatch.model.ProcessableZone;
import overwatch.model.Zone;
import overwatch.service.*;
import overwatch.skeleton.Outline;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Engine {

    private static final class CancelHock implements BooleanSupplier {

        private boolean isCanceled = false;

        public void cancel(){
            isCanceled = true;
        }

        @Override
        public boolean getAsBoolean() {
            return isCanceled;
        }
    }

    private static final @NotNull ReentrantLock threadModificationLock = new ReentrantLock();

    private static final @NotNull Logger logger = Logger.getLogger(Engine.class.getName());

    private static volatile @Nullable Thread engineThread;

    private static volatile @Nullable ObjectAnalyserTask engineTask;

    private static volatile @Nullable Engine.CancelHock engineCancelHook;

    private Engine () {}

    public static boolean isRunning(){
        final Thread engineThread = Engine.engineThread;
        return engineThread != null && engineThread.isAlive();
    }

    public static boolean isStopped(){
        final Thread engineThread = Engine.engineThread;
        return engineThread == null || !engineThread.isAlive();
    }

    public static boolean isCanceled(){
        if(isStopped()) return false;
        final CancelHock cancelHock = Engine.engineCancelHook;
        return cancelHock != null && cancelHock.isCanceled;
    }

    public static void cancel(){
        if(isStopped()) return;
        threadModificationLock.lock();
        final CancelHock engineCancelHook = Engine.engineCancelHook;
        if(engineCancelHook != null)
            engineCancelHook.cancel();
        threadModificationLock.unlock();
    }

    public static void start(Zone[] zones) {
        threadModificationLock.lock();
        long startTimestamp = System.currentTimeMillis();
        cancel();

        // ImageService sollte Quelldaten neu laden.
        Arrays.stream(zones)
                .parallel()
                .map(Zone::capture)
                .distinct()
                .forEach(ImageService::updateSourceImage);

        // Setze neue Engine auf.
        final CancelHock engineCancelHook = new CancelHock();
        final ObjectAnalyserTask engineTask = new ObjectAnalyserTask(zones, engineCancelHook);
        final Thread engineThread =  new Thread(engineTask);
        engineThread.setDaemon(false);
        engineThread.start();

        Engine.engineTask = engineTask;
        Engine.engineCancelHook = engineCancelHook;
        Engine.engineThread = engineThread;

        threadModificationLock.unlock();
        long endTimestamp = System.currentTimeMillis();
        logger.info("Engine restarted in " + (endTimestamp - startTimestamp) + "ms.");
    }

    public static BufferedImage getGeneratedImage(){
        final ObjectAnalyserTask objectAnalyserTask = Engine.engineTask;
        return isRunning() && objectAnalyserTask != null
                ? objectAnalyserTask.getImage()
                : new BufferedImage(1,1, BufferedImage.TYPE_INT_RGB);
    }

    private static class ObjectAnalyserTask implements Runnable {

        private static final int UNMODIFIED_PIXEL_RGB = Color.white.getRGB();

        private static final int MODIFIED_PIXEL_RGB = Color.black.getRGB();

        private static final int ZONE_BOUNDS_RGB = Color.blue.getRGB();

        private static final int OUTLINES_RGB = Color.red.getRGB();

        private static final int ZONE_TAKEN_RGB = Color.green.getRGB();

        private final long iterationInterval;

        private final @NotNull Outline outerBounds;

        private final @NotNull BooleanSupplier isCanceled;

        private final @NotNull ProcessableZone[] zones;

        private final @NotNull Capture[] captures;

        private final @NotNull BufferedImage generatedImage;

        private volatile @NotNull @UnmodifiableView List<Outline> outlines = List.of();

        private volatile @NotNull @UnmodifiableView Collection<Zone> zonesWithObjects = Set.of();

        private ObjectAnalyserTask(@NotNull Zone[] zones, @NotNull BooleanSupplier isCanceled) {
            this.zones = Arrays.stream(zones)
                    .map(ProcessableZone::new)
                    .toArray(ProcessableZone[]::new);
            this.captures = Arrays.stream(zones)
                    .map(Zone::capture)
                    .distinct()
                    .toArray(Capture[]::new);
            this.iterationInterval = ConfigurationService.getInt(ConfigurationService.Keys.ANALYSE_INTERVAL_MS);
            this.isCanceled = isCanceled;
            this.outerBounds = Outline.compose(zones);
            this.generatedImage = new BufferedImage(outerBounds.width(), outerBounds.height(), BufferedImage.TYPE_INT_RGB);
        }

        private void zonesWithObjectChanged(Collection<Zone> zonesWithObjects){
            this.zonesWithObjects = zonesWithObjects;
            HttpService.sendZoneUpdate(zonesWithObjects.stream().mapToInt(Zone::nr).toArray());
        }

        public BufferedImage getImage(){
            updateGeneratedImage();
            return generatedImage;
        }

        private synchronized void updateGeneratedImage(){
            final long generateImageBeginnTimestamp = System.currentTimeMillis();
            final @Nullable List<Outline> outlines = this.outlines;
            final @Nullable Collection<Zone> zonesWithObjects = this.zonesWithObjects;
            final @NotNull Graphics g = generatedImage.getGraphics();

            // Reset
            IntStream.range(0, outerBounds.width())
                    .parallel()
                    .forEach(x -> IntStream.range(0, outerBounds.height())
                            .parallel()
                            .forEach(y -> generatedImage.setRGB(x,y, UNMODIFIED_PIXEL_RGB)));

            // Besetzte Zonen
            g.setColor(new Color(ZONE_TAKEN_RGB));
            for (Zone zone : zonesWithObjects)
                g.fillRect(zone.x(), zone.y(), zone.width(), zone.height());

            // Zonen Umriss
            g.setColor(new Color(ZONE_BOUNDS_RGB));
            for (Zone zone : zones) {
                g.drawRect(zone.x(), zone.y(), zone.width(), zone.height());
                g.drawString(Integer.toString(zone.nr()), zone.x() + 5, zone.y() + 10);
            }

            // Objekte
            for (Outline outline : outlines) {
                g.setColor(new Color(OUTLINES_RGB));
                g.drawRect(outline.x(), outline.y(), outline.width(), outline.height());
                g.setColor(new Color(MODIFIED_PIXEL_RGB));
                IntStream.rangeClosed(outline.x(), outline.endX())
                        .forEach(x -> {
                            IntStream.rangeClosed(outline.y(), outline.endY())
                                    .filter(y -> ZoneService.calculatePixelState(x,y, this.zones, null).isModified)
                                    .forEach(y -> {
                                        g.fillRect(x,y,1,1);
                                    });
                        });
            }
            g.dispose();

            final long generateImageFinishedTimestamp = System.currentTimeMillis();
            final long generateImageDurationMillis = generateImageFinishedTimestamp - generateImageBeginnTimestamp;
            logger.info("Generate-image took: '" + generateImageDurationMillis  + "' ms.");
        }


        @Override
        public void run() throws RuntimeException {
            while (!isCanceled.getAsBoolean()){
                final long analyseBeginnTimestamp = System.currentTimeMillis();
                Arrays.stream(captures).forEach(ImageService::updateCurrentImage);
                Arrays.stream(zones).parallel().forEach(ProcessableZone::reset);
                final List<Outline> outlines = this.outlines = Collections.unmodifiableList(ObjectAnalyserService.findObjects(zones));
                final Collection<Zone> zonesWithObjects = Collections.unmodifiableCollection(ObjectAnalyserService.findZonesWithObject(zones, outlines));

                // Change Detection
                if(zonesWithObjects.size() != this.zonesWithObjects.size()) {
                    zonesWithObjectChanged(zonesWithObjects);
                }
                else {
                    for (Zone a : zonesWithObjects) {
                        if(!this.zonesWithObjects.contains(a)) {
                            zonesWithObjectChanged(zonesWithObjects);
                            break;
                        }
                    }
                }
                final long analyseFinishedTimestamp = System.currentTimeMillis();
                final long analyseDurationMillis = analyseFinishedTimestamp - analyseBeginnTimestamp;
                logger.info("Analyse-iteration took: '" + analyseDurationMillis  + "' ms.");

                if(iterationInterval > analyseDurationMillis) {
                    long sleepMillis = iterationInterval - analyseDurationMillis;
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    logger.warning("Analyse-iteration took: '" + analyseDurationMillis  + "' ms. This is longer than an iteration should take.");
                }
            }
        }
    }
}
