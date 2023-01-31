package overwatch.service;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Simpler Zugriff auf Konfigurationen.
 */
public class ConfigurationService {

    /**
     * Die verfügbaren Schlüssel.
     */
    public interface Keys {
        String SERVER_PORT = "server-port";

        String IMAGE_BASE_PATH = "image-base-path";

        String ANALYSE_INTERVAL_MS = "analyse-interval-ms";

        String ANALYSE_OPENCV_ENABLE = "analyse-opencv-enable";

        String DEBUG_FRAME_ENABLE = "debug-frame";

        String SERVICE_UPDATE_ENDPOINT = "service-update-endpoint";
        String SERVICE_UPDATE_KEY = "service-update-key";

        String OVERWATCH_INIT_KEY = "overwatch-init-key";
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

    public static void override(String key, String value){
        properties.put(key, value);
    }

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
     * Liest eine Eigenschaft anhand eines Schlüssels als long.
     * @param key Der zu lesende Schlüssel.
     * @return Gibt den verknüpften Wert zurück. Sollte der Wert nicht vorhanden sein, wird per Default 0 übergeben.
     */
    public static long getLong(String key) {
        String value = properties.getProperty(key);
        return value == null ? 0 : Long.parseLong(value);
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

    /**
     * Liest eine Eigenschaft anhand eines Schlüssels als boolean.
     * @param key Der zu lesende Schlüssel.
     * @return Gibt den verknüpften Wert zurück. Sollte der Wert nicht vorhanden sein, wird per Default false übergeben.
     */
    public static boolean getBoolean(String key) {
        String value = properties.getProperty(key);
        return Boolean.parseBoolean(value);
    }
}
