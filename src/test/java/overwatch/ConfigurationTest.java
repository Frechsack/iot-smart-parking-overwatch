package overwatch;

import org.junit.Assert;
import org.junit.Test;
import overwatch.service.ConfigurationService;

public class ConfigurationTest {

    @Test
    public void serverPort(){
        Assert.assertEquals(ConfigurationService.getInt(ConfigurationService.Keys.SERVER_PORT), 8081);
    }
}
