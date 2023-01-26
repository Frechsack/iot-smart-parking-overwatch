package overwatch;

import org.junit.Test;

import java.awt.*;
import java.util.Collection;

public class PixelTest {

    private boolean isPixelDifferent(int source, int current){

        final double SIGNIFICATION = 0.5;

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

        double difference = Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
        System.out.println(difference);


        return difference > SIGNIFICATION;
    }

    @Test
    public void test(){
        isPixelDifferent(new Color(10,10,10).getRGB(), new Color(50,20,20).getRGB());
    }


}
