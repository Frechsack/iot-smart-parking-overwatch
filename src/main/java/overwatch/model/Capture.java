package overwatch.model;

import org.jetbrains.annotations.NotNull;
import overwatch.skeleton.Outline;

/**
 * Stellt ein Videogerät dar, von welchen ein Bild gemacht werden kann.
 */
public record Capture(
        int x,
        int y,
        int width,
        int height,
        int endX,
        int endY,
        int area,
        @NotNull String deviceName) implements Outline {

    /**
     * Name für Captures, welche als virtuell angesehen werden sollen.
     */
    public static String VIRTUAL_CAMERA_NAME = "Virtual";

    public Capture(int x, int y, int width, int height, @NotNull String deviceName){
        this(x, y, width, height, x + width - 1, y + height -1, width * height, deviceName);
    }

    /**
     * Prüft, ob dieses Gerät eine virtuelle Kamera ist und mit vorgefertigten Bildern versorgt werden soll.
     * @return {@code true}, wenn diese Kamera virtuell ist.
     */
    public boolean isVirtual(){
        return VIRTUAL_CAMERA_NAME.equals(deviceName);
    }

    @Override
    public int endX() {
        return endX;
    }

    @Override
    public int endY() {
        return endY;
    }

    @Override
    public int area() {
        return area;
    }
}
