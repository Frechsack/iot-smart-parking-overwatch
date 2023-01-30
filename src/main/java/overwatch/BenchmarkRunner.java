package overwatch;

import org.openjdk.jmh.annotations.*;
import overwatch.algorithm.Algorithm;
import overwatch.model.Capture;
import overwatch.model.Zone;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BenchmarkRunner {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 2, time = 800, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 2, time = 800, timeUnit =  TimeUnit.MILLISECONDS)
    @Fork(value = 2, warmups = 3)
    public void compute(){

        Capture c1 = new Capture(0,0,800,400, "/dev/video0");
        Zone z1 = new Zone(1, c1, 0,0, 200,200);
        Zone z2 = new Zone(2, c1, 200,0, 200,200);
        Zone z3 = new Zone(3, c1, 400,0, 200,200);
        Zone z4 = new Zone(4, c1,600,0, 200,200);
        Zone z5 = new Zone(5, c1,0,200, 200,200);
        Zone z6 = new Zone(6, c1,200,200, 200,200);
        Zone z7 = new Zone(7, c1,400,200, 200,200);
        Zone z8 = new Zone(8, c1,600,200, 200,200);

        Zone[] zones = new Zone[]{
                z1,z2,z3,z4,z5,z6,z7,z8
        };

        Algorithm algorithm = Algorithm.create(zones);
        algorithm.compute();
        algorithm.close();
    }
}
