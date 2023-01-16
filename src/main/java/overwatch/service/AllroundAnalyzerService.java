package overwatch.service;

import overwatch.model.ProcessableZone;
import overwatch.model.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllroundAnalyzerService {

    private static final int SKIP_PIXELS = 5;
    private static final int CHECK_SURROUNDING_PIXELS_RADIUS = 5;


    public static int [] analyzeZone(Zone [] zones){
        ProcessableZone[] processableZones = new ProcessableZone[zones.length];
        for(int i = 0; i < zones.length; i++){
            processableZones[i] = new ProcessableZone(zones[i]);
        }
        List<Integer> takenZones = new ArrayList<Integer>();
        List<Runnable> actions = new ArrayList<Runnable>();

        // TODO: [zoneNr]
        return new int[2];
    }


    private static boolean isTaken(ProcessableZone[] zones, ProcessableZone zone){
        for(int x = 1; x <= zone.width; x+=SKIP_PIXELS) {
            for(int y = 1; y <= zone.height; y+=SKIP_PIXELS) {

                if(zone.isModified(x,y)) {


                }

            }

            }
return false;
    }



    private static final ExecutorService exec = Executors.newCachedThreadPool();
}
