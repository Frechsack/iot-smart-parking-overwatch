package overwatch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import overwatch.model.Capture;
import overwatch.model.ProcessableZone;
import overwatch.model.Zone;
import overwatch.service.ConfigurationService;
import overwatch.service.ImageService;
import overwatch.service.ObjectAnalyserService;
import overwatch.skeleton.Outline;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;

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

    private static @Nullable Thread engineThread;

    private static @Nullable Engine.CancelHock engineCancelHook;

    private Engine () {}

    public static boolean isRunning(){
        return engineThread != null && engineThread.isAlive();
    }

    public static boolean isStopped(){
        return engineThread == null || !engineThread.isAlive();
    }

    public static boolean isCanceled(){
        if(isStopped()) return false;
        CancelHock cancelHock = Engine.engineCancelHook;
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
        engineCancelHook = new CancelHock();
        engineThread = new Thread(new ObjectAnalyserTask(zones, engineCancelHook));
        engineThread.setDaemon(false);
        engineThread.start();
        threadModificationLock.unlock();
        long endTimestamp = System.currentTimeMillis();
        logger.info("Engine restarted in " + (endTimestamp - startTimestamp) + "ms.");
    }

    private static void  zonesChanged(Zone[] zones){
        /// TODO: Http-Client Backend
    }

    private static class ObjectAnalyserTask implements Runnable {

        private final long iterationInterval;

        private final @NotNull BooleanSupplier isCanceled;

        private final @NotNull ProcessableZone[] zones;

        private final @NotNull Capture[] captures;

        private @Nullable Collection<Zone> zonesWithObjects;

        private ObjectAnalyserTask(@NotNull Zone[] zones, @NotNull BooleanSupplier isCanceled) {
            this.zones = Arrays.stream(zones)
                    .map(ProcessableZone::new)
                    .toArray(ProcessableZone[]::new);
            this.captures = Arrays.stream(zones)
                    .map(Zone::capture)
                    .toArray(Capture[]::new);
            this.iterationInterval = ConfigurationService.getInt(ConfigurationService.Keys.ANALYSE_INTERVAL_MS);
            this.isCanceled = isCanceled;
        }

        private void zonesWithObjectChanged(Collection<Zone> zonesWithObject){
            Engine.zonesChanged((this.zonesWithObjects = zonesWithObject).toArray(Zone[]::new));
        }

        @Override
        public void run() throws RuntimeException {
            while (!isCanceled.getAsBoolean()){

                // Aktualisiere Bilddaten
                Arrays.stream(captures).parallel().forEach(ImageService::updateCurrentImage);
                long iterationStartTimestamp = System.currentTimeMillis();
                Arrays.stream(zones).parallel().forEach(ProcessableZone::reset);
                final Collection<Outline> outlines = ObjectAnalyserService.findObjects(zones);
                final Collection<Zone> zonesWithObject = ObjectAnalyserService.findZonesWithObject(zones, outlines);

                // Change Detection
                if(this.zonesWithObjects == null || (zonesWithObject.size() != this.zonesWithObjects.size())) {
                    zonesWithObjectChanged(zonesWithObject);
                }
                else {
                    for (Zone a : zonesWithObject) {
                        if(!this.zonesWithObjects.contains(a)) {
                            zonesWithObjectChanged(zonesWithObject);
                            break;
                        }
                    }
                }

                // Sleep
                long iterationEndTimestamp = System.currentTimeMillis();
                long iterationTakenMillis = iterationEndTimestamp - iterationStartTimestamp;
                logger.info("Analyse-iteration took: '" + iterationTakenMillis  + "' ms.");
                if(iterationInterval > iterationTakenMillis) {
                    long sleepMillis = iterationInterval - iterationTakenMillis;
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    logger.warning("Analyse-iteration took: '" + iterationTakenMillis  + "' ms. This is longer than an iteration should take.");
                }
            }
        }
    }
}
