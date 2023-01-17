package overwatch.model;

public class Capture {

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public Capture(int x, int y, int width, int height, String deviceName) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.deviceName = deviceName;
    }

    public final String deviceName;
}
