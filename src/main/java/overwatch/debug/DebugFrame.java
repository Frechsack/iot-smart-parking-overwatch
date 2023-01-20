package overwatch.debug;

import overwatch.service.ObjectAnalyserService;
import overwatch.skeleton.Outline;
import overwatch.model.*;
import overwatch.service.ZoneService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DebugFrame extends JFrame {

    // TODO: Mehrere Caputures anzeigen.
    private final Outline outline;
    private final ProcessableZone[] zones;

    private final List<Outline> objects;

    private class RootPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for(ProcessableZone zone : zones) {
                g.setColor(Color.BLUE);
                g.drawRect(zone.x(), zone.y(), zone.width(), zone.height());
                g.drawString(Integer.toString(zone.nr()), zone.x() + 5, zone.y() + 10);
            }
            for(int x = outline.x(); x < outline.width(); x++){
                for(int y = outline.y(); y < outline.height(); y++){
                    if(ZoneService.calculatePixelState(x, y, zones, null).isModified) {
                        g.setColor(Color.black);
                        g.fillRect(x,y,1,1);
                    }
                }
            }

            g.setColor(Color.RED);
            for (Outline object: objects){
                g.drawRect(object.x(), object.y(), object.width(), object.height());
            }
        }
    }

    public DebugFrame (Zone[] zones){
        super();
        this.outline = Outline.compose(zones);
        this.zones = new ProcessableZone[zones.length];
        for (int i = 0; i < zones.length; i++)
            this.zones[i] = new ProcessableZone(zones[i]);

        this.objects = ObjectAnalyserService.findObjects(this.zones);

        setSize(outline.width() + 2, outline.height() + 40);
        RootPanel root = new RootPanel();
        root.setBounds(0, 0, outline.width(), outline.height());
        setContentPane(root);
        setVisible(true);
    }

}
