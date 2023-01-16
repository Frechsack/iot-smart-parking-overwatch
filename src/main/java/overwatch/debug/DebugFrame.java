package overwatch.debug;

import overwatch.model.Capture;
import overwatch.model.Zone;

import javax.swing.*;

public class DebugFrame extends JFrame {

    public DebugFrame (Capture capture, Zone[] zones){
        super();
        setSize(capture.width, capture.height);
        setVisible(true);
        JPanel root = new JPanel();
        root.setSize(capture.width, capture.height);
        root.setLocation(0,0);
        setContentPane(root);



    }

}
