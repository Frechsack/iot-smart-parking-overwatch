package overwatch.dto;

import org.junit.Assert;
import org.junit.Test;
import overwatch.model.Capture;
import overwatch.model.Zone;

public class InitDtoTest {

    @Test
    public void toZones() {
        CapturesDto capturesDto = new CapturesDto();
        capturesDto.addCapture("/dev/video0", new CaptureDto(0,0,50,50));
        ZonesDto zonesDto = new ZonesDto();
        zonesDto.addZone("1", new ZoneDto(0,0,50,50,"/dev/video0"));

        InitDto initDto = new InitDto(capturesDto, zonesDto);

        Zone[] zones = initDto.toZones();
        Capture expectedCapture = new Capture(0,0,50,50,"/dev/video0");
        Zone expected = new Zone(1,expectedCapture,0,0,50,50);

        Assert.assertEquals(expected, zones[0]);
    }
}