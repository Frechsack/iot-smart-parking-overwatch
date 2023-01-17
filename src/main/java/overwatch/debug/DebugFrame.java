package overwatch.debug;

import overwatch.model.Capture;
import overwatch.model.ProcessableZone;
import overwatch.model.Zone;
import overwatch.service.ObjectAnalyserService;

import javax.swing.*;
import java.awt.*;

public class DebugFrame extends JFrame {

    // TODO: Mehrere Caputures anzeigen.
    private final Capture capture;
    private final ProcessableZone[] processableZones;

    private class RootPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for(ProcessableZone processableZone : processableZones) {
                g.setColor(Color.blue);
                g.drawRect(processableZone.absoluteXStart, processableZone.absoluteYStart, processableZone.width, processableZone.height);
                g.setFont(Font.getFont("Arial"));
                g.drawString(Integer.toString(processableZone.nr), processableZone.absoluteXStart + 8, processableZone.absoluteYStart + 16);

                g.setColor(Color.BLACK);
                for(int x = 0; x < processableZone.width; x++){
                    for (int y = 0; y <  processableZone.height; y++) {
                        if(processableZone.isModified(x,y))
                            g.drawRect(processableZone.offsetX +x , processableZone.offsetY + y, 1 , 1);
                    }
                }

                int[] positions = ObjectAnalyserService.findOuterBounds(120,140, processableZone, processableZones);
                int absoluteMinX = positions[0], absoluteMinY = positions[1], absoluteMaxX = positions[2], absoluteMaxY = positions[3];
                g.setColor(Color.RED);
                g.drawRect(absoluteMinX, absoluteMinY, absoluteMaxX - absoluteMinX, absoluteMaxY - absoluteMinY);

                positions = ObjectAnalyserService.findOuterBounds(60,50, processableZone, processableZones);
                absoluteMinX = positions[0];
                absoluteMinY = positions[1];
                absoluteMaxX = positions[2];
                absoluteMaxY = positions[3];
                g.setColor(Color.RED);
                g.drawRect(absoluteMinX, absoluteMinY, absoluteMaxX - absoluteMinX, absoluteMaxY - absoluteMinY);

            }
        }
    }

    public DebugFrame (Capture capture, Zone[] zones){
        super();
        this.capture = capture;
        this.processableZones = new ProcessableZone[zones.length];
        for (int i = 0; i < zones.length; i++)
            this.processableZones[i] = new ProcessableZone(zones[i]);

        setSize(capture.width + 2, capture.height + 40);
        RootPanel root = new RootPanel();
        root.setBounds(0, 0, capture.width, capture.height);
        setContentPane(root);
        setVisible(true);
    }

}
