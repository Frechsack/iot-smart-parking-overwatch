package overwatch;

import org.junit.Assert;
import org.junit.Test;
import overwatch.model.Zone;

import static org.junit.Assert.*;

public class EngineTest {

    @Test
    public void isRunning() {
        Engine.start(new Zone[0]);
        Assert.assertTrue(Engine.isRunning());
    }

    @Test
    public void isStopped() {
        Engine.start(new Zone[0]);
        Engine.cancel();
        long startTimestamp = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTimestamp < 2000 && !Engine.isStopped()){
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Assert.assertTrue(Engine.isStopped());
    }

    @Test
    public void isCanceled() {
        Engine.start(new Zone[0]);
        Assert.assertFalse(Engine.isCanceled());
        Engine.cancel();
        Assert.assertTrue(Engine.isCanceled());
    }

    @Test
    public void cancel() {
        isCanceled();
    }

    @Test
    public void start() {
        Assert.assertFalse(Engine.isRunning());
        Engine.start(new Zone[0]);
        Assert.assertTrue(Engine.isRunning());
    }

    @Test
    public void getGeneratedImage() {
        assertNotNull(Engine.getGeneratedImage());
    }
}