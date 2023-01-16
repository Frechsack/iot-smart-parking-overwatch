package overwatch.model;

public class Zone {

    public final int nr;
    public final int offsetX;
    public final int offsetY;
    public final int width;
    public final int height;
    public final Capture capture;

    public Zone(int nr, int offsetX, int offsetY, int width, int height, Capture capture) {
        this.nr = nr;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
        this.capture = capture;

    }



}


