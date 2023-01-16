package overwatch;

import java.awt.image.BufferedImage;
import java.nio.Buffer;

public class Image {

    public final BufferedImage source;

    public Image(BufferedImage source) {
        this.source = source;
        this.width = source.getWidth();
        this.height = source.getHeight();
    }

    public final int width;
    public final int height;

    public int getPixel(int x, int y){
        return source.getRGB(x,y);
    }



}
