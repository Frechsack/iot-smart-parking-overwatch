package overwatch.service;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Simpler Zugriff auf Konfigurationen.
 */
public class ConfigurationService {

    public interface Keys {
        String SERVER_PORT = "server-port";

        String IMAGE_BASE_PATH = "/tmp/";
    }

    private static final String CONFIGURATION_PATH = "src/main/resources/application.properties";

    private static final java.util.Properties properties = new Properties();

    private static final Logger logger = Logger.getLogger(ConfigurationService.class.getName());

    static {
        try {
            properties.load(new FileReader(CONFIGURATION_PATH));
            logger.info("ConfigurationService applied");
        }
        catch (IOException e) {
            logger.severe(e.getMessage());
            logger.severe("Program will exit");
            System.exit(0);
        }
    }

    private ConfigurationService(){}

    /**
     * Liest eine Eigenschaft anhand eines Schlüssels.
     * @param key Der zu lesende Schlüssel.
     * @return Gibt den verknüpften Wert oder einen leeren String zurück.
     */
    public static String getString(String key) {
        return properties.getProperty(key, "");
    }

    /**
     * Liest eine Eigenschaft anhand eines Schlüssels als integer.
     * @param key Der zu lesende Schlüssel.
     * @return Gibt den verknüpften Wert zurück. Sollte der Wert nicht vorhanden sein, wird per Default 0 übergeben.
     */
    public static int getInt(String key) {
        String value = properties.getProperty(key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    /**
     * Liest eine Eigenschaft anhand eines Schlüssels als float.
     * @param key Der zu lesende Schlüssel.
     * @return Gibt den verknüpften Wert zurück. Sollte der Wert nicht vorhanden sein, wird per Default 0 übergeben.
     */
    public static float getFloat(String key) {
        String value = properties.getProperty(key);
        return value == null ? 0 : Float.parseFloat(key);
    }
}
