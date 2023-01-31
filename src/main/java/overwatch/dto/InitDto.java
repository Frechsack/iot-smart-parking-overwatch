package overwatch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import overwatch.model.Capture;
import overwatch.model.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Transportobjekt für den Init Aufruf.
 */
public class InitDto {
    @JsonProperty(required = true)
    private CapturesDto captures;
    @JsonProperty(required = true)
    private ZonesDto zones;

    public InitDto(@NotNull CapturesDto captures, @NotNull ZonesDto zones) {
        this.captures = captures;
        this.zones = zones;
    }

    public InitDto(){}

    /**
     * Transformiert den Inhalt dieses DTOs in Zonen.
     * @return Gibt alle Zonen als Array zurück.
     * @throws RuntimeException Wird geworfen, sollte eine Zone auf eine nicht vorhandene {@link Capture} verweisen.
     */
    public @NotNull Zone[] toZones() throws RuntimeException {
        final List<Capture> captures = new ArrayList<>();
        for (Map.Entry<String, CaptureDto> entry : this.captures.getCaptures().entrySet()) {
            final String deviceName = entry.getKey();
            final CaptureDto dto = entry.getValue();
            captures.add(new Capture(dto.getX(), dto.getY(), dto.getWidth(), dto.getHeight(), deviceName));
        }
        final List<Zone> zones = new ArrayList<>();
        for (Map.Entry<Integer, ZoneDto> entry : this.zones.getZones().entrySet()) {
            final int nr = entry.getKey();
            final ZoneDto dto = entry.getValue();
            Capture capture = null;
            for (Capture o : captures)
                if(o.deviceName().equals(dto.getDeviceName())){
                    capture = o;
                    break;
                }
            if(capture == null)
                throw new IllegalArgumentException("Capture with device: '" + dto.getDeviceName() + "' is not found.");
            zones.add(new Zone( nr, capture, dto.getOffsetX(), dto.getOffsetY(), dto.getWidth(), dto.getHeight()));
        }

        return zones.toArray(Zone[]::new);
    }
}
