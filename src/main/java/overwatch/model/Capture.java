package overwatch.model;

import org.jetbrains.annotations.NotNull;
import overwatch.skeleton.Outline;

/**
 * Stellt ein Videogerät dar, von welchen ein Bild gemacht werden kann.
 */
public record Capture(int x, int y, int width, int height, int endX, int endY, int area, @NotNull String deviceName) implements Outline {

    public static String VIRTUAL_CAMERA_NAME = "Virtual";

    public Capture(int x, int y, int width, int height, @NotNull String deviceName){
        this(x, y, width, height, x + width - 1, y + height -1, width * height, deviceName);
    }

    public boolean isVirtual(){
        return VIRTUAL_CAMERA_NAME.equals(deviceName);
    }
}
