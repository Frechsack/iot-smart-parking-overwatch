package overwatch.service;

import overwatch.skeleton.Outline;
import overwatch.model.*;

import java.util.*;

public class AllroundAnalyzerService {

    private static final int SKIP_PIXELS = 5;
    private static final int SIGNIFICANT_AREA_TO_DETECT = 25;

    public static List<Outline> findObjects(final ProcessableZone[] zones){
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


    private static boolean isIntersecting(Outline a, Outline b) {
        return a.endY() >= b.y() &&
                a.y() <= b.endY() &&
                a.x() <= b.endX() &&
                a.endX() >= b.x();
    }

    private static int intersectionArea(Outline a, Outline b){
        int x = Math.max(a.x(), b.x());
        int y = Math.max(a.y(), b.y());
        int endX = Math.min(a.endX(), b.endX());
        int endY = Math.min(a.endY(), b.endY());
        int width = endX - x;
        int height = endY - y;
        return width * height;
    }
}
