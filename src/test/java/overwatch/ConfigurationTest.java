package overwatch;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void serverPort(){
        Assert.assertEquals(Configuration.getInt(Configuration.Keys.SERVER_PORT), 8081);
    }
}
