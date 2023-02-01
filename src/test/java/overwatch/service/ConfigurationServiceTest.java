package overwatch.service;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationServiceTest {

    @Test
    public void override() {
        ConfigurationService.override("Key", "Value");
        Assert.assertEquals("Value", ConfigurationService.getString("Key"));
    }

    @Test
    public void getString() {
        Assert.assertEquals("/home/frechsack/", ConfigurationService.getString(ConfigurationService.Keys.IMAGE_BASE_PATH));
    }

    @Test
    public void getInt() {
        Assert.assertEquals(1, ConfigurationService.getInt(ConfigurationService.Keys.SERVER_PORT));
    }

    @Test
    public void getLong() {
        Assert.assertEquals(100, ConfigurationService.getInt(ConfigurationService.Keys.ANALYSE_INTERVAL_MS));
    }

    @Test
    public void getFloat() {
        Assert.assertEquals(100, ConfigurationService.getInt(ConfigurationService.Keys.ANALYSE_INTERVAL_MS));
    }

    @Test
    public void getBoolean() {
        Assert.assertTrue(ConfigurationService.getBoolean(ConfigurationService.Keys.ANALYSE_OPENCV_ENABLE));
    }
}