package overwatch.model;

import overwatch.service.ImageService;
import overwatch.skeleton.Image;
import overwatch.skeleton.Outline;

import java.util.Arrays;

/**
 * Eine spezialisierte Variante von {@link Zone}, welche Pixeldaten beinhält.
 */
public final class ProcessableZone extends Zone implements Outline {

    /**
     * Wichtig: Muss null entsprechen, weil null als default für shorts gilt und auf diese Logik zurückgegriffen wird.
     */
    private static final short UNSET = 0;

    /**
     * Ein Pixel ist modifiziert.
     */
    private static final short MODIFIED = 1;

    /**
     * Ein Pixel ist nicht modifiziert.
     */
    private static final short UNMODIFIED = 2;

    /**
     * Der zu erreichende Schwellwert, damit zwei Pixel als verschieden und somit {@link #MODIFIED} gelten.
     */
    private static final float BRIGHTNESS_DELTA = 0.01f;

    /**
     * Die aktuellen Status der Pixel.
     */
    private final short [] pixelStates;

    public ProcessableZone(Zone zone) {
        super(zone);
        this.pixelStates = new short[zone.width() * zone.height()];
    }

    /**
     * Setzt alle Pixeldaten zurück. Sie nehmen den Zustand {@link #UNSET} an.
     */
    public void reset(){
        Arrays.fill(pixelStates, (short) 0);
    }

    /**
     * Prüft, ob ein Pixel modifiziert wurde.
     * @param relativeX Die Position auf der x-Achse.
     * @param relativeY Die Position auf der y-Achse.
     * @return Gibt {@code true} zurück, sollte der Pixel modifiziert sein, andernfalls {@code false}.
     */
    public boolean isModified(int relativeX, int relativeY){
        int index = relativeX + relativeY * width();
        if (pixelStates[index] == UNSET){
            return processPixel(relativeX,relativeY,index);
        }
        return pixelStates[index] == MODIFIED;
    }

    /**
     * Berechnung eines Pixelzustandes und Ausgabe, ob dieser {@link #MODIFIED} ist.
     * @param x Die Position auf der x-Achse.
     * @param y Die Position auf der y-Achse.
     * @param index Der berechnete Index für die Speicherposition des Pixels.
     * @return Gibt aus, ob der Pixel {@link #MODIFIED} oder {@link #UNMODIFIED} ist.
     */
    private boolean processPixel(int x, int y, int index){
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

        return ((float) cmax) / 255.0f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProcessableZone zone = (ProcessableZone) o;
        return Arrays.equals(pixelStates, zone.pixelStates);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(pixelStates);
        return result;
    }

    @Override
    public String toString() {
        return "ProcessableZone{" +
                "nr=" + nr +
                ", capture=" + capture +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", endX=" + endX +
                ", endY=" + endY +
                ", area=" + area +
                '}';
    }
}

