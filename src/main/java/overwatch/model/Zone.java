package overwatch.model;

import org.jetbrains.annotations.NotNull;
import overwatch.skeleton.Outline;

import java.util.Objects;

/**
 * Eine virtuelle Unterteilung eines Bildes.
 */
public class Zone implements Outline {

    protected final int nr;

    protected final @NotNull Capture capture;
    protected final int offsetX;
    protected final int offsetY;
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;
    protected final int endX;
    protected final int endY;
    protected final int area;

    public Zone(int nr, @NotNull Capture capture, int offsetX, int offsetY, int width, int height) {
        this.nr = nr;
        this.capture = capture;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.x = capture.x() + offsetX;
        this.y = capture.y() + offsetY;
        this.width = width;
        this.height = height;
        this.endX = capture.x() + offsetX + width - 1;
        this.endY = capture.y() + offsetY + height - 1;
        this.area = width * height;
    }

    public Zone(Zone copy){
        this(copy.nr, copy.capture, copy.offsetX, copy.offsetY, copy.width, copy.height);
    }

    public int nr(){
        return nr;
    }

    public @NotNull Capture capture(){
        return capture;
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
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int area() {
        return area;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zone zone = (Zone) o;
        return nr == zone.nr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nr);
    }
}


