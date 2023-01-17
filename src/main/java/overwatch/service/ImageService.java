package overwatch.service;

import overwatch.Configuration;
import overwatch.Constants;
import overwatch.Image;
import overwatch.model.Capture;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Statische Methoden zum Ein- und auslesen von Bildern.
 */
public class ImageService {

    private static final Logger logger = Logger.getLogger(ImageService.class.getName());
    private static final Map<String, Image> sourceImageMap = new ConcurrentHashMap<>();
    private static final Map<String, Image> currentImageMap = new ConcurrentHashMap<>();

    private ImageService() {}

    private static Image readImageFromIO(Capture capture, boolean isSourceImage) throws Exception {
        boolean isVirtual = Constants.VIRTUAL_CAMERA_NAME.equals(capture.deviceName);
        final var imagePath = !isVirtual
                ? Configuration.getString(Configuration.Keys.IMAGE_BASE_PATH) + "/" +  capture.deviceName + ".png"
                : isSourceImage
                    ? "src/main/resources/ImageSource.png"
                    : "src/main/resources/ImageCurrent.png";

        if(!isVirtual) {
            final var command = new String[]{ "fswebcam", "-d", capture.deviceName, "--png", "1", "-q", imagePath };
            Runtime.getRuntime().exec(command).waitFor();
        }
        return new Image.BackedImage(ImageIO.read(new File(imagePath)));
    }

    private static Image createBlank(int width, int height) {
        return new Image.BlankImage(width, height);
    }

    /**
     * Liest den Bildspeicher für das Quellbild erneut ein.
     * @param capture Das Videogerät für den Einlesevorgang.
     */
    public static void updateSourceImage(Capture capture){
        try {
            sourceImageMap.put(capture.deviceName, readImageFromIO(capture, true));
        } catch (Exception e) {
            logger.severe(e.getMessage());
            if (sourceImageMap.containsKey(capture.deviceName)) return;

            sourceImageMap.put(capture.deviceName, createBlank(capture.width, capture.height));
        }
    }

    /**
     * Liest den Bildspeicher für das aktuelle Referenzbild erneut ein.
     * @param capture Das Videogerät für den Einlesevorgang.
     */
    public static void updateCurrentImage(Capture capture) {
        try {
            currentImageMap.put(capture.deviceName, readImageFromIO(capture, false));
        }
        catch (Exception e){
            logger.severe(e.getMessage());
            if(currentImageMap.containsKey(capture.deviceName)) return;

            currentImageMap.put(capture.deviceName, createBlank(capture.width, capture.height));
        }
    }

    /**
     * Liest das aktuelle Referenzbild aus. Sollte noch kein Bild angelegt worden sein, wird ein Schwarzes Bild in passender Größer ausgegeben.
     * @param capture Das verknüpfte Videogerät.
     * @return Gibt das aktuelle Referenzbild aus.
     */
    public static Image readCurrentImage(Capture capture){
        Image image = currentImageMap.get(capture.deviceName);
        return image == null ? createBlank(capture.width, capture.height) : image;
    }

    /**
     * Liest das aktuelle Quellbild aus. Sollte noch kein Bild angelegt worden sein, wird ein Schwarzes Bild in passender Größer ausgegeben.
     * @param capture Das verknüpfte Videogerät.
     * @return Gibt das aktuelle Referenzbild aus.
     */
    public static Image readSourceImage(Capture capture){
        Image image = sourceImageMap.get(capture.deviceName);
        return image == null ? createBlank(capture.width, capture.height) : image;
    }
}
