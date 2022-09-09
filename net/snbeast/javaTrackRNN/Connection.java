package net.snbeast.javaTrackRNN;

import java.awt.*;
import java.io.*;

public class Connection implements Serializable {
    public final double strength;
    public final Node inputNode;
    public final Color color;
    public Connection (double strength, Node inputNode) {
        this.strength = strength;
        this.inputNode = inputNode;
        float alpha = (float)Math.abs(strength);
        if (strength < 0) color = new Color(0, 0, 1, alpha);
        else color = new Color(1, 0, 0, alpha);
    }
    public double getOutput () {
        return inputNode.getOutput() * strength;
    }
}
