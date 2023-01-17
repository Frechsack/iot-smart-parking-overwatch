package overwatch.model;

/**
 * Stellt ein Videogerät dar, von welchen ein Bild gemacht werden kann.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class Capture {

    /**
     * Die virtuelle Positionierung des Bildes im Parkhaus auf der x-Achse.
     */
    public final int x;

    /**
     * Die virtuelle Positionierung des Bildes im Parkhaus auf der y-Achse.
     */
    public final int y;

    /**
     * Die Breite des Bildes.
     */
    public final int width;

    /**
     * Die Höhe des Bildes.
     */
    public final int height;

    public Capture(int x, int y, int width, int height, String deviceName) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.deviceName = deviceName;
    }

    /**
     * Der Name des Videogerätes.
     */
    public final String deviceName;
}
