package overwatch.model;

/**
 * Eine virtuelle Unterteilung eines Bildes.
 */
public class Zone {

    /**
     * Die Nr der Zone.
     */
    public final int nr;

    /**
    Die Positionierung der Zone auf der x-Achse innerhalb seiner {@link Capture}.
    */
    public final int offsetX;

    /**
     Die Positionierung der Zone auf der y-Achse innerhalb seiner {@link Capture}.
     */
    public final int offsetY;

    /**
     * Die Breite der Zone auf der x-Achse.
     */
    public final int width;

    /**
     * Die Höhe der Zone auf der y-Achse.
     */
    public final int height;

    /**
     * Das zugewiesene {@link Capture}.
     */
    public final Capture capture;

    /**
     * Die kleinste Koordinate auf der x-Achse in absoluten Koordinaten.
     */
    public final int absoluteXStart;

    /**
     * Die größte Koordinate auf der x-Achse in absoluten Koordinaten.
     */
    public final int absoluteXEnd;

    /**
     * Die kleinste Koordinate auf der y-Achse in absoluten Koordinaten.
     */
    public final int absoluteYStart;

    /**
     * Die größte Koordinate auf der y-Achse in absoluten Koordinaten.
     */
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

        if(this.width > capture.width)
            throw new IllegalStateException("Zone is widder than it´s capture.");
        if(this.height > capture.height)
            throw new IllegalStateException("Zone is higher than it´s capture.");
    }
}


