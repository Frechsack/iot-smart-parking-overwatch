package overwatch.service;

import overwatch.model.ProcessedZone;
import overwatch.model.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllroundAnalyzerService {

    private static final int SKIP_PIXELS = 5;
    private static final int CHECK_SURROUNDING_PIXELS_RADIUS = 5;


    public static int [] analyzeZone(Zone [] zones){
        ProcessedZone[] processedZones = new ProcessedZone[zones.length];
        for(int i = 0; i < zones.length; i++){
            processedZones[i] = new ProcessedZone(zones[i]);
        }
        List<Integer> takenZones = new ArrayList<Integer>();
        List<Runnable> actions = new ArrayList<Runnable>();
            }


    private static boolean isTaken(ProcessedZone [] zones, ProcessedZone zone){
        for(int x = 1; x <= zone.zone.width; x+=SKIP_PIXELS) {
            for(int y = 1; y <= zone.zone.height; y+=SKIP_PIXELS) {

                if(zone.isModified(x,y)) {


                }

            }

            }

    }



    private static final ExecutorService exec = Executors.newCachedThreadPool();
}
