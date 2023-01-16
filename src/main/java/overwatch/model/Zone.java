package overwatch.model;

public class Zone {

    public final int nr;
    public final int offsetX;
    public final int offsetY;
    public final int width;
    public final int height;
    public final Capture capture;

    /*
    * Calculated
    */
    public final int absoluteXStart;
    public final int absoluteXEnd;
    public final int absoluteYStart;
    public final int absoluteYEnd;

    public Zone(Zone copy) {
        this(copy.nr, copy.offsetX, copy.offsetY, copy.width, copy.height, copy.capture);
    }

    public Zone(int nr, int offsetX, int offsetY, int width, int height, Capture capture) {
        this.nr = nr;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
        this.capture = capture;
        absoluteXStart = offsetX + capture.x;
        absoluteYStart = offsetY + capture.y;


        absoluteXEnd = absoluteXStart + width - 1;
        absoluteYEnd = absoluteYStart + height - 1;
    }
}


