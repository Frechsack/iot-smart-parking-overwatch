package overwatch;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Simpler Zugriff auf Konfigurationen.
 */
public class Configuration {

    interface Keys {
        String SERVER_PORT = "server-port";
    }

    private static final String CONFIGURATION_PATH = "src/main/resources/application.properties";

    private static final java.util.Properties properties = new Properties();

    private static final Logger logger = Logger.getLogger(Configuration.class.getName());

    static {
        try {
            properties.load(new FileReader(CONFIGURATION_PATH));
            logger.info("Configuration applied");
        }
        catch (IOException e) {
            logger.severe(e.getMessage());
            logger.severe("Program will exit");
            System.exit(0);
        }
    }

    private Configuration(){}

    /**
     * Liest eine Eigenschaft anhand eines Schlüssels.
     * @param key Der zu lesende Schlüssel.
     * @param defaultValue Ein Ersatzwert, sollte der übergebene Schlüssel nicht vorhanden sein.
     * @return Gibt den verknüpften Wert oder den Ersatzwert zurück.
     */
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
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
