package overwatch.debug;

import overwatch.Engine;
import overwatch.model.Zone;
import overwatch.service.ConfigurationService;
import overwatch.skeleton.Outline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DebugFrame extends JFrame {

    private static class RootPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

           g.drawImage(Engine.getGeneratedImage(),0,0, null);
        }
    }

    public DebugFrame (Zone[] zones){
        super();
        Outline outline = Outline.compose(zones);
        setSize(outline.width() + 2, outline.height() + 40);
        RootPanel root = new RootPanel();
        root.setBounds(0, 0, outline.width(), outline.height());
        setContentPane(root);
        setVisible(true);

        Timer timer = new Timer(ConfigurationService.getInt(ConfigurationService.Keys.ANALYSE_INTERVAL_MS), e -> {
            root.repaint();
        });
        timer.setInitialDelay(1000);
        timer.setRepeats(true);
        timer.start();
    }

}
