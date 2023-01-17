package overwatch.model;

import overwatch.Image;
import overwatch.service.ImageService;

public class ProcessableZone extends Zone {

    private static final short UNSET = 0;
    private static final short MODIFIED = 1;
    private static final short UNMODIFIED = 2;

    private static final float BRIGHTNESS_DELTA = 0.01f;

    private final short [] pixelStates;

    public ProcessableZone(Zone zone) {
        super(zone);
        this.pixelStates = new short[width * height];
    }

    public ProcessableZone(int nr, int offsetX, int offsetY, int width, int height, Capture capture) {
        super(nr, offsetX, offsetY, width, height, capture);
        this.pixelStates = new short[width * height];
    }



    public boolean isModified(int x, int y){
        int index = (x-1) + (y-1) * width;
        if (pixelStates[index] == UNSET){
            return processedPixel(x,y,index);
        }
        return pixelStates[index] == MODIFIED;
    }

    private synchronized boolean processedPixel(int x, int y, int index){
        if (pixelStates[index] != UNSET) {
            return pixelStates[index] == MODIFIED;
        }

        Image sourceImage = ImageService.readSourceImage(capture);
        Image currentImage = ImageService.readCurrentImage(capture);

        int offsetX = this.offsetX + x;
        int offsetY = this.offsetY + y;
        int sourcePixel = sourceImage.getPixel(offsetX, offsetY);
        int currentPixel = currentImage.getPixel(offsetX, offsetY);

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

        int cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        int cmin = Math.min(r, g);
        if (b < cmin) cmin = b;

        return ((float) cmax) / 255.0f;
    }
}

