package overwatch.debug;

import overwatch.Image;
import overwatch.model.Capture;
import overwatch.model.ProcessedZone;
import overwatch.model.Zone;
import overwatch.service.ImageService;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;

public class DebugFrame extends JFrame {

    private final Capture capture;
    private final Zone[] zones;

    private class RootPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            for(Zone zone : zones) {
                ProcessedZone processedZone = new ProcessedZone(zone);
                for(int x = 1; x <= zone.width; x++){
                    for (int y = 1; y <= zone.height; y++) {
                        if(processedZone.isModified(x,y))
                            g.drawRect(zone.offsetX +x , zone.offsetY + y, 1 , 1);
                    }
                }
            }
        }
    }

    public DebugFrame (Capture capture, Zone[] zones){
        super();
        this.capture = capture;
        this.zones = zones;
        setSize(capture.width, capture.height);
        RootPanel root = new RootPanel();
        root.setBounds(0, 0, capture.width, capture.height);
        setContentPane(root);
        setVisible(true);
    }

}
