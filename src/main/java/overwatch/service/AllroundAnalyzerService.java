package overwatch.service;

import overwatch.Bounds;
import overwatch.model.ProcessableZone;
import overwatch.model.Zone;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class AllroundAnalyzerService {

    private static final int SKIP_PIXELS = 5;
    private static final int CHECK_SURROUNDING_PIXELS_RADIUS = 5;
    private static final int SIGNIFICANT_AREA_TO_DETECT = 25;


    public static Zone[] zoneWithObject(Zone [] zones){
        ProcessableZone[] processableZones = new ProcessableZone[zones.length];
        for(int i = 0; i < zones.length; i++){
            processableZones[i] = new ProcessableZone(zones[i]);
        }
        var actions = Arrays.stream(processableZones).map(it -> (Callable<Zone>) () -> {
            if(containsObject(processableZones, it)){
                return it;
            }else{
                return null;
            }
        }).toList();
        try {
            List<Future<Zone>> results = exec.invokeAll(actions);
            return results.stream().map(it-> {
                try {
                    return it.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).filter(Objects::nonNull).toArray(Zone[]::new);
        } catch (InterruptedException e) {
            return new Zone [0];
        }
    }


    private static boolean isColliding(Zone objA, Bounds objB) {
        if (objA.absoluteYEnd >= objB.y() &&
                objA.absoluteYStart <= objB.lastY() &&
                objA.absoluteXStart <= objB.lastX() &&
                objA.absoluteXEnd >= objB.x()) {
            return true;
        }
        return false;
    }

    private static int collidingArea(Zone zone, Bounds bounds){
        int x = Math.max(zone.absoluteXStart, bounds.x());
        int y = Math.max(zone.absoluteYStart, bounds.y());
        int xEnd = Math.min(zone.absoluteXEnd, bounds.lastX());
        int yEnd = Math.min(zone.absoluteYEnd, bounds.lastY());
        int width = xEnd - x;
        int height = yEnd - y;
        return width * height;
    }
    private static boolean containsObject(ProcessableZone[] zones, ProcessableZone zone){
        record Area(ProcessableZone zone,int area){};
        for(int x = 0; x < zone.width; x+=SKIP_PIXELS) {
            for(int y = 0; y < zone.height; y+=SKIP_PIXELS) {

                if(zone.isModified(x,y)) {
                    Bounds bounds = ObjectAnalyserService.findOuterBounds(x,y,zone,zones);
                    if(bounds.area() <= SIGNIFICANT_AREA_TO_DETECT){
                        continue;
                    }
                    ProcessableZone matchingZone = Arrays.stream(zones).filter(it->isColliding(it,bounds))
                            .map(it->new Area(it,collidingArea(it,bounds)))
                            .max(Comparator.comparingInt(o -> o.area))
                            .map(it->it.zone)
                            .orElse(null);
                    if(matchingZone == zone){
                        return true;
                    }
                }
            }
        }
        return false;
    }



    private static final ExecutorService exec = Executors.newCachedThreadPool();
}
