package overwatch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoneDto {
    @JsonProperty(required = true)
    private int offsetX;
    @JsonProperty(required = true)
    private int offsetY;
    @JsonProperty(required = true)
    private int width;
    @JsonProperty(required = true)
    private int height;

    @JsonProperty(required = true)
    private String deviceName;

    public ZoneDto() {
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
