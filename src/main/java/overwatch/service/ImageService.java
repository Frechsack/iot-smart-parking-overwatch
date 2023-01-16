package overwatch.service;

import overwatch.Image;
import overwatch.model.Capture;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageService {

    public static Image readImage(Capture capture){
        try {
            return new Image(ImageIO.read(new File("src\\main\\resources\\ImageBackground.png")));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Image readBackgroundImage(Capture capture){
        try {
            return new Image(ImageIO.read(new File("src\\main\\resources\\ImageSz1.png")));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
