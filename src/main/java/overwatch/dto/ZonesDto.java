package overwatch.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class ZonesDto {

    private final Map<Integer, ZoneDto> zones = new HashMap<>();

    @JsonAnySetter
    void addZone(String key, ZoneDto zone) {
        zones.put(Integer.parseInt(key), zone);
    }

    public Map<Integer, ZoneDto> getZones() {
        return zones;
    }
}
