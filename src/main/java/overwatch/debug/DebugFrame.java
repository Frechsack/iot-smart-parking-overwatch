package overwatch.debug;

import overwatch.model.Capture;
import overwatch.model.ProcessableZone;
import overwatch.model.Zone;
import overwatch.service.RunForPixelsService;

import javax.swing.*;
import java.awt.*;

public class DebugFrame extends JFrame {

    private final Capture capture;
    private final Zone[] zones;

    private class RootPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            for(Zone zone : zones) {
                ProcessableZone processableZone = new ProcessableZone(zone);
                for(int x = 1; x <= zone.width; x++){
                    for (int y = 1; y <= zone.height; y++) {
                        if(processableZone.isModified(x,y))
                            g.drawRect(zone.offsetX +x , zone.offsetY + y, 1 , 1);
                    }
                }
                int[] positions = RunForPixelsService.findOuterBounds(0,0, processableZone,)
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
