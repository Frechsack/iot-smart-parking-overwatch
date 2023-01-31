package overwatch.algorithm.dongle;

import overwatch.model.Capture;
import overwatch.service.ConfigurationService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Service für den {@link DongleAlgorithm} um auf Bilddaten zuzugreifen.
 */
class DongleImageService {

    private static final Logger logger = Logger.getLogger(DongleImageService.class.getName());
    private static final Map<String, DongleImage> sourceImageMap = new ConcurrentHashMap<>();
    private static final Map<String, DongleImage> currentImageMap = new ConcurrentHashMap<>();

    private DongleImageService() {}

    static DongleImage readImageFromIO(Capture capture, boolean isSourceImage) throws Exception {
        boolean isVirtual = capture.isVirtual();
        final var imagePath = !isVirtual
                ? ConfigurationService.getString(ConfigurationService.Keys.IMAGE_BASE_PATH) + "/" +  capture.deviceName().replace("/", "_") + ".png"
                : isSourceImage
                ? "src/main/resources/ImageSource.png"
                : "src/main/resources/ImageCurrent.png";

        if(!isVirtual) {
            final var command = new String[]{ "fswebcam", "-d", capture.deviceName(),"-q", imagePath };
            final var saturation = new String[]{ "convert", "-blur", "0x5", imagePath, imagePath};
            Runtime.getRuntime().exec(command).waitFor();
            Runtime.getRuntime().exec(saturation).waitFor();
        }

        BufferedImage image = ImageIO.read(new File(imagePath));

        if(image.getWidth() != capture.width() || image.getHeight() != capture.height()) {
            if (isSourceImage)
                logger.warning("DongleImage-Dimension of device: '" + capture.deviceName() +"' does not match size of capture. Picture will be scaled.");

            BufferedImage scaled = new BufferedImage(capture.width(), capture.height(), image.getType());
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, capture.width(), capture.height(), 0, 0, image.getWidth(),
                    image.getHeight(), null);
            g.dispose();
            image.flush();
            image = scaled;
        }
        return new DongleImage.BackedImage(image);
    }

    private static DongleImage createBlank(int width, int height) {
        return new DongleImage.BlankImage(width, height);
    }

    /**
     * Liest den Bildspeicher für das Quellbild erneut ein.
     * @param capture Das Videogerät für den Einlesevorgang.
     */
    static void updateSourceImage(Capture capture){
        try {
            DongleImage oldImage = sourceImageMap.put(capture.deviceName(), readImageFromIO(capture, true));
            if(oldImage != null)
                oldImage.flush();
        } catch (Exception e) {
            logger.severe(e.getMessage());
            if (sourceImageMap.containsKey(capture.deviceName())) return;

            DongleImage oldImage = sourceImageMap.put(capture.deviceName(), createBlank(capture.width(), capture.height()));
            if(oldImage != null)
                oldImage.flush();
        }
    }

    /**
     * Liest den Bildspeicher für das aktuelle Referenzbild erneut ein.
     * @param capture Das Videogerät für den Einlesevorgang.
     */
    static void updateCurrentImage(Capture capture) {
        try {
            DongleImage oldImage = currentImageMap.put(capture.deviceName(), readImageFromIO(capture, false));
            if(oldImage != null)
                oldImage.flush();
        }
        catch (Exception e){
            logger.severe(e.getMessage());
            if(currentImageMap.containsKey(capture.deviceName())) return;

            DongleImage oldImage = currentImageMap.put(capture.deviceName(), createBlank(capture.width(), capture.height()));
            if(oldImage != null)
                oldImage.flush();
        }
    }

    /**
     * Liest das aktuelle Referenzbild aus. Sollte noch kein Bild angelegt worden sein, wird ein Schwarzes Bild in passender Größer ausgegeben.
     * @param capture Das verknüpfte Videogerät.
     * @return Gibt das aktuelle Referenzbild aus.
     */
    static DongleImage readCurrentImage(Capture capture){
        DongleImage image = currentImageMap.get(capture.deviceName());
        return image == null ? createBlank(capture.width(), capture.height()) : image;
    }

    /**
     * Liest das aktuelle Quellbild aus. Sollte noch kein Bild angelegt worden sein, wird ein Schwarzes Bild in passender Größer ausgegeben.
     * @param capture Das verknüpfte Videogerät.
     * @return Gibt das aktuelle Referenzbild aus.
     */
    static DongleImage readSourceImage(Capture capture){
        DongleImage image = sourceImageMap.get(capture.deviceName());
        return image == null ? createBlank(capture.width(), capture.height()) : image;
    }
}
