package overwatch.model;

import overwatch.service.ImageService;
import overwatch.skeleton.Image;
import overwatch.skeleton.Outline;

import java.util.Arrays;
import java.util.Objects;

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

        short pixelState = isPixelDifferent(sourcePixel, currentPixel)
                ? MODIFIED
                :UNMODIFIED;
        pixelStates[index] = pixelState;
        return pixelState == MODIFIED;

    }

    private boolean isPixelDifferent(int source, int current){
        final double SIGNIFICANT = 150;

        int sR = (source & 0xff0000) >> 16;
        int sB = source & 0xff;
        int sG = (source & 0xff00) >> 8;
        int cR = (current & 0xff0000) >> 16;
        int cB = current & 0xff;
        int cG = (current & 0xff00) >> 8;
        int rmean = (sR +cR )/ 2;
        int r = sR - cR;
        int g = sG - cG;
        int b = sB - cB;
        return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8)) > SIGNIFICANT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProcessableZone zone = (ProcessableZone) o;
        return nr == zone.nr;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nr);
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

