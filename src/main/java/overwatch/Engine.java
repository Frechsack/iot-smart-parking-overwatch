package overwatch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import overwatch.algorithm.Algorithm;
import overwatch.debug.DebugFrame;
import overwatch.model.Zone;
import overwatch.service.*;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Engine welche eine Instanz für den Bewegungserkennungsalgorithmus steuert.
 */
public class Engine {

    /**
     * Interner Hook um einen Algorithmus zu stoppen.
     */
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

    private static volatile @Nullable Engine.EngineTask engineTask;

    private static volatile @Nullable Engine.CancelHock engineCancelHook;

    private static final @Nullable DebugFrame debugFrame;

    private Engine () {}

    static {
        debugFrame = ConfigurationService.getBoolean(ConfigurationService.Keys.DEBUG_FRAME_ENABLE)
                ? new DebugFrame()
                : null;
    }

    /**
     * Prüft, ob der Algorithmus aktuell läuft.
     * @return Gibt {@code true} zurück, wenn der Algorithmus läuft, sonst {@code false}.
     */
    public static boolean isRunning(){
        final Thread engineThread = Engine.engineThread;
        return engineThread != null && engineThread.isAlive();
    }

    /**
     * Prüft, ob der Algorithmus aktuell gestoppt ist.
     * @return Gibt {@code true} zurück, wenn der Algorithmus läuft, sonst {@code false}.
     */
    public static boolean isStopped(){
        final Thread engineThread = Engine.engineThread;
        return engineThread == null || !engineThread.isAlive();
    }

    /**
     * Prüft, ob der Algorithmus abgebrochen werden soll. Dieser Zustand ist erreicht, wenn der Algorithmus läuft und {@link #cancel()} aufgerufen wurde.
     * @return Gibt {@code true} zurück, wenn der Algorithmus abgebrochen werden soll, sonst {@code false}.
     */
    public static boolean isCanceled(){
        if(isStopped()) return false;
        final CancelHock cancelHock = Engine.engineCancelHook;
        return cancelHock != null && cancelHock.isCanceled;
    }

    /**
     * Stoppt den aktuellen Algorithmus, sollte dieser aktuell laufen.
     */
    public static void cancel(){
        if(isStopped()) return;
        threadModificationLock.lock();
        final CancelHock engineCancelHook = Engine.engineCancelHook;
        if(engineCancelHook != null)
            engineCancelHook.cancel();
        threadModificationLock.unlock();
    }

    /**
     * Startet eine neue Instanz des Algorithmus. Sollte aktuell ein Algorithmus laufen, wird dieser abgebrochen {@link #cancel()}.
     * Es wird nicht auf den Abbruch gewartet, es wird sofort eine neue Instanz gestartet.
     * @param zones Die auszuwertenden Zonen.
     */
    public static void start(Zone[] zones) {
        threadModificationLock.lock();
        long startTimestamp = System.currentTimeMillis();
        cancel();
        // Setze neue Engine auf.
        final CancelHock engineCancelHook = new CancelHock();
        final EngineTask engineTask = new EngineTask(zones, engineCancelHook);
        final Thread engineThread =  new Thread(engineTask);
        engineThread.setDaemon(false);
        engineThread.start();

        Engine.engineTask = engineTask;
        Engine.engineCancelHook = engineCancelHook;
        Engine.engineThread = engineThread;

        threadModificationLock.unlock();
        long endTimestamp = System.currentTimeMillis();
        logger.info("Engine restarted in " + (endTimestamp - startTimestamp) + "ms.");

        if (debugFrame != null)
            debugFrame.updateZones(zones);
    }

    /**
     * Liest das aktuelle Bild aus dem laufenden Algorithmus aus.
     * Sollte aktuell kein Algorithmus laufen, wird ein leeres Bild mit einer Größe von 1x1 ausgegeben.
     * @return Gibt das aktuelle Bild zurück.
     */
    public static BufferedImage getGeneratedImage(){
        final EngineTask engineTask = Engine.engineTask;
        return isRunning() && engineTask != null
                ? engineTask.getImage()
                : new BufferedImage(1,1, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Abstrahierung für einen Thread.
     */
    private static class EngineTask implements Runnable {

        private final long iterationInterval;

        /**
         * Callback um zu prüfen, ob dieser Task abgebrochen wurde.
         */
        private final @NotNull BooleanSupplier isCanceled;

        /**
         * Der Anzuwendende Algorithmus.
         */
        private final @NotNull Algorithm algorithm;

        /**
         * Liste mit aktiven Zonen in der letzten Iteration.
         */
        private @NotNull @UnmodifiableView Collection<? extends Zone> activeZones = Set.of();

        /**
         * Array mit Listen der letzten aktiven Zonen. Anhand dieser wird geprüft, ob und wie sich die Elemente in {@link #activeZones} verändert haben.
         */
        @SuppressWarnings("unchecked")
        private final Collection<? extends Zone>[] activeZonesStack =
                (Collection<? extends Zone>[]) IntStream.range(0, calculateHistorySize()).boxed().map(it -> List.of()).toArray(Collection[]::new);


        private EngineTask(@NotNull Zone[] zones, @NotNull BooleanSupplier isCanceled) {
            this.iterationInterval = ConfigurationService.getInt(ConfigurationService.Keys.ANALYSE_INTERVAL_MS);
            this.isCanceled = isCanceled;
            this.algorithm = Algorithm.create(zones);
        }

        private static int calculateHistorySize(){
            int interval = ConfigurationService.getInt(ConfigurationService.Keys.ANALYSE_INTERVAL_MS);
            if(interval > 0){
                float techSize = 1500f / (float)interval;
                int size = (int) techSize;
                return Math.max(size, 1);
            }
            return 4;
        }

        /**
         * Vergleicht zwei Collections miteinander.
         * Zwei Collections sind identisch, wenn alle Elemente aus a in b und alle Elemente aus b in a vorkommen.
         * @param a Die erste Collection.
         * @param b Die zweite Collection.
         * @return Gibt {@code true} zurück, sollten beide Collections identisch sein.
         */
        private boolean isEqual(Collection<?> a, Collection<?> b){
            return b.containsAll(a) && a.containsAll(b);
        }

        private synchronized void updateActiveZones(Collection<? extends Zone> newZones){
            final @NotNull @UnmodifiableView Collection<? extends Zone> activeZones = this.activeZones;
            final Predicate<Zone> isInStack = zone -> {
                for (Collection<?> stackItem : activeZonesStack) {
                    if(!stackItem.contains(zone))
                        return false;
                }
                return true;
            };

            final @NotNull Set<? extends Zone> newActiveZones = newZones.stream()
                    .filter(isInStack)
                    .collect(Collectors.toSet());

            if(!isEqual(newActiveZones, activeZones)){
                HttpService.sendActiveZones(newActiveZones.stream().mapToInt(Zone::nr).toArray());
                this.activeZones = Collections.unmodifiableSet(newActiveZones);
            }

            // Update Stack
            for (int i = 0; i < activeZonesStack.length - 1; i++)
                activeZonesStack[i] = activeZonesStack[i+1];
            activeZonesStack[activeZonesStack.length-1] = newZones;
        }

        public BufferedImage getImage(){
            return algorithm.computeImage();
        }

        @Override
        public void run() throws RuntimeException {
            while (!isCanceled.getAsBoolean()){
                final long analyseBeginnTimestamp = System.currentTimeMillis();
                final Collection<? extends Zone> newZones = algorithm.compute();
                updateActiveZones(newZones);
                final long analyseFinishedTimestamp = System.currentTimeMillis();
                final long analyseDurationMillis = analyseFinishedTimestamp - analyseBeginnTimestamp;

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
            algorithm.close();
        }
    }
}
