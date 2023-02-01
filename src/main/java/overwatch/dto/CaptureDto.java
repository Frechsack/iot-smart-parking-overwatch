package overwatch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Transportobjekt für Videogeräte.
 */
public class CaptureDto {
    @JsonProperty(required = true)
    private int x;
    @JsonProperty(required = true)
    private int y;
    @JsonProperty(required = true)
    private int width;
    @JsonProperty(required = true)
    private int height;

    public CaptureDto() {
    }

    public CaptureDto(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
