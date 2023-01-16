package overwatch.service;

import overwatch.Configuration;
import overwatch.Constants;
import overwatch.Image;
import overwatch.model.Capture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ImageService {

    private static final Logger logger = Logger.getLogger(ImageService.class.getName());

    private static final Map<String, Image> sourceImageMap = new ConcurrentHashMap<>();
    private static final Map<String, Image> currentImageMap = new ConcurrentHashMap<>();

    private ImageService() {}

    private static Image readImageFromIO(Capture capture, boolean isSourceImage) throws Exception {
        final var imagePath = !Constants.VIRTUAL_CAMERA_NAME.equals(capture.deviceName)
                ? Configuration.getString(Configuration.Keys.IMAGE_BASE_PATH) + "/" +  capture.deviceName + ".png"
                : isSourceImage
                    ? "src/main/resources/ImageSource.png"
                    : "src/main/resources/ImageCurrent.png";
        // TODO: Pfad
        final var command = new String[]{ "fswebcam", "-d", capture.deviceName, "--png", "1", "-q", imagePath };
        Runtime.getRuntime().exec(command).waitFor();
        return new Image(ImageIO.read(new File(imagePath)));
    }

    private static Image createBlank(int width, int height) {
        return new Image(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));
    }

    public static void updateSourceImage(Capture capture){
        try {
            sourceImageMap.put(capture.deviceName, readImageFromIO(capture, true));
        }
        catch (Exception e) {
            logger.severe(e.getMessage());
            if(sourceImageMap.containsKey(capture.deviceName)) return;

            sourceImageMap.put(capture.deviceName, createBlank(capture.width, capture.height));

        }
    }

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

    public static Image readCurrentImage(Capture capture){
        return currentImageMap.get(capture.deviceName);
    }

    public static Image readSourceImage(Capture capture){
        return sourceImageMap.get(capture.deviceName);
    }
}
