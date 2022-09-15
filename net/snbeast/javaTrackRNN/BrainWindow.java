package net.snbeast.javaTrackRNN;

import java.awt.*;
import javax.swing.*;

public class BrainWindow extends JPanel {
    private Brain brain;

    public BrainWindow (Brain brain) {
        this.brain = brain;
        setPreferredSize(new Dimension(Brain.panelWidth, Brain.panelHeight));
    }

    public void setBrain (Brain brain) {
        this.brain = brain;
    }

    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent(g);
        brain.draw(g);
    }
}
