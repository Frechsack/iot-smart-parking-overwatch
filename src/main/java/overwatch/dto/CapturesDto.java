package overwatch.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class CapturesDto {

    private final Map<String, CaptureDto> captures = new HashMap<>();
    @JsonAnySetter
    void addCapture(String key, CaptureDto capture){
        captures.put(key, capture);
    }

    public Map<String, CaptureDto> getCaptures() {
        return captures;
    }
}
