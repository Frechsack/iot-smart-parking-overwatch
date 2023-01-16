package overwatch.model;

import overwatch.Image;
import overwatch.service.ImageService;

public class ProcessedZone {

    private static final short UNSET = 0;
    private static final short MODIFIED = 1;
    private static final short UNMODIFIED = 2;

    private static final float BRIGHTNESS_DELTA = 0.2f;

    private final short [] pixelStates;
    public final Zone zone;


    public ProcessedZone(Zone zone) {
        this.zone = zone;
        this.pixelStates = new short[zone.width * zone.height];

    }

    public boolean isModified(int x, int y){
        int index = x + (y-1) * zone.width;
        if (pixelStates[index] == UNSET){
            return processedPixel(x,y,index);

        }
        return pixelStates[index] == MODIFIED;
    }

    private synchronized boolean processedPixel(int x, int y, int index){
        if (pixelStates[index] != UNSET) {
            return pixelStates[index] == MODIFIED;
        }

        Image sourceImage = ImageService.readBackgroundImage(zone.capture);
        Image currentImage = ImageService.readImage(zone.capture);

        int offSetX = zone.offsetX + x;
        int offSetY = zone.offsetY + y;
        int sourcePixel = sourceImage.getPixel(offSetX, offSetY);
        int currentPixel = currentImage.getPixel(offSetX, offSetY);

        float sourceBrightness = calculateBrightness(sourcePixel);
        float currentBrightness = calculateBrightness(currentPixel);

        short pixelState = Math.abs(sourceBrightness) - Math.abs(currentBrightness) > BRIGHTNESS_DELTA
                ? MODIFIED
                :UNMODIFIED;
        pixelStates[index] = pixelState;
        return pixelState == MODIFIED;

    }

    private float calculateBrightness(int rgb) {
        int r = (rgb & 0xff0000) >> 16;
        int b = rgb & 0xff;
        int g = (rgb & 0xff00) >> 8;

        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        return ((float) cmax) / 255.0f;
    }
}

