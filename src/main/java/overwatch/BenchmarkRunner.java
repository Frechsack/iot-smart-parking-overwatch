package overwatch;

import org.openjdk.jmh.annotations.*;
import overwatch.model.Capture;
import overwatch.model.ProcessableZone;
import overwatch.model.Zone;
import overwatch.service.AllroundAnalyzerService;
import overwatch.service.ImageService;
import overwatch.service.ObjectAnalyserService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BenchmarkRunner {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 3, time = 100, timeUnit =  TimeUnit.MILLISECONDS)
    @Fork(value = 2, warmups = 2)
    public void findObjects(){

        Capture capture = new Capture(0,0,800,400, Capture.VIRTUAL_CAMERA_NAME);
        Zone z1 = new Zone(1, capture, 0,0, 200,200);
        Zone z2 = new Zone(2, capture, 200,0, 200,200);
        Zone z3 = new Zone(3, capture, 400,0, 200,200);
        Zone z4 = new Zone(4, capture,600,0, 200,200);
        Zone z5 = new Zone(5, capture,0,200, 200,200);
        Zone z6 = new Zone(6, capture,200,200, 200,200);
        Zone z7 = new Zone(7, capture,400,200, 200,200);
        Zone z8 = new Zone(8, capture,600,200, 200,200);

        Zone[] zones = new Zone[]{
                z1,z2,z3,z4,z5,z6,z7,z8
        };

        ImageService.updateSourceImage(capture);
        ImageService.updateCurrentImage(capture);

        ProcessableZone[] processableZones = new ProcessableZone[zones.length];
        for (int i = 0; i < processableZones.length; i++) {
            processableZones[i] = new ProcessableZone(zones[i]);
        }
        AllroundAnalyzerService.findObjects(processableZones);

    }
}
