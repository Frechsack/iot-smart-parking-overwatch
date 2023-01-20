package overwatch.skeleton;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

/**
 * Interface für Bilder, welche über eine größe Verfügen und lesbare Pixel.
 */
public interface Image extends Size {

    /**
     * Die Breite des Bildes.
     * @return Die Breits des Bildes.
     */
    int width();

    /**
     * Die Höhe des Bildes.
     * @return Die Höhe des Bildes.
     */
    int height();

    /**
     * Liest Pixel aus.
     * @param x Die Position auf der x-Achse.
     * @param y Die Position auf der y-Achse.
     * @return Gibt die Farbe des Pixels in {@link BufferedImage#TYPE_INT_RGB} aus.
     */
    int getPixel(int x, int y);

    /**
     * Ein leeres Bild mit einer festen Größe. Alle Pixel sind einfarbig Schwarz.
     * @param width Die Breite des Bildes.
     * @param height Die Höhe des Bildes.
     */
    record BlankImage(int width, int height) implements Image {

        @Override
        public int getPixel(int x, int y) {
            return 0;
        }
    }

    /**
     * Ein Bild, welches im Hintergrund ein {@link BufferedImage} verwendet.
     */
    class BackedImage implements Image {

        private final @NotNull BufferedImage source;

        public BackedImage(@NotNull BufferedImage source) {
            this.source = source;
        }

        @Override
        public int width() {
            return source.getWidth();
        }

        @Override
        public int height() {
            return source.getHeight();
        }

        @Override
        public int getPixel(int x, int y) {
            return source.getRGB(x,y);
        }
    }

}
