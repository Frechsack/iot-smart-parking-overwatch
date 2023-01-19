package overwatch.model;

import org.jetbrains.annotations.NotNull;
import overwatch.skeleton.Outline;

/**
 * Eine virtuelle Unterteilung eines Bildes.
 */
public record Zone(int nr, @NotNull Capture capture, int offsetX, int offsetY, int x, int y, int width, int height, int endX, int endY, int area) implements Outline {

    public Zone(int nr, @NotNull Capture capture, int offsetX, int offsetY, int width, int height) {
        this(nr, capture, offsetX, offsetY, capture.x() + offsetX, capture.y() + offsetY, width, height, capture.x() + offsetX + width - 1, capture.y() + offsetY + height - 1, width * height);
    }

}


