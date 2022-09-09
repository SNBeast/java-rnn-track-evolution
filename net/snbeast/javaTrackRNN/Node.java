package net.snbeast.javaTrackRNN;

import java.awt.*;
import java.io.*;

public class Node implements Serializable {
    private final Connection[] inputConnections;
    private final Brain brain;
    private double input = 0;
    private double output = 0;
    private Color color = Color.BLACK;
    private boolean lastCycleProcessed = true;

    // if inputConnections is null, the input is supplied
    public Node (Connection[] inputConnections, Brain brain) {
        this.inputConnections = inputConnections;
        this.brain = brain;
    }

    public void setInput (double input) {
        this.input = input;
    }

    public double getOutput () {
        if (lastCycleProcessed == brain.getCurrentCycle()) {
            return output;
        }
        lastCycleProcessed = brain.getCurrentCycle();
        return processOutput();
    }
    private double processOutput () {
        if (inputConnections == null) {
            output = input;
            if (output < 0) color = new Color(0, 0, -(float)output);
            else color = new Color((float)output, 0, 0);
            return output;
        }
        double out = 0;
        for (int i = 0; i < inputConnections.length; i++) {
            out += inputConnections[i].getOutput();
        }
        output = Utils.sigmoid(out);
        if (output < 0) color = new Color(0, 0, -(float)output);
        else color = new Color((float)output, 0, 0);
        return output;
    }

    public double getConnectionStrength (int connection) {
        return inputConnections[connection].strength;
    }
    public Color getConnectionColor (int connection) {
        return inputConnections[connection].color;
    }
    public Color getColor () {
        return color;
    }
}
