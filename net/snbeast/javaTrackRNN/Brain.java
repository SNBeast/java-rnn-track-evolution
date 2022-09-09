package net.snbeast.javaTrackRNN;

import java.awt.*;
import java.io.*;

public class Brain implements Serializable {
    public static final int inputNodeCount = 9;
    public static final int outputNodeCount = 2;
    private static final int[] intermediateNodeDimensions = {5};
    public static final int panelWidth = 200;
    public static final int panelHeight = 200;
    private static final int radius = 10;
    private static final int diameter = radius * 2;

    private Node[] inputNodes = new Node[inputNodeCount];
    private Node[] outputNodes = new Node[outputNodeCount];
    private Node[][] intermediateNodes;
    private double mutability = 0.2;

    // a binary tracker used by the nodes to evaluate whether a node's output was calculated already in the current brain process
    private boolean currentCycle = false;

    public static Brain readBrainFromFile (String s) throws FileNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));) {
            return (Brain)ois.readObject();
        } catch (Exception e) {
            if (!(e instanceof FileNotFoundException)) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return null;
    }

    public Brain (Brain brainToInheritFrom) {
        for (int i = 0; i < inputNodes.length; i++) {
            inputNodes[i] = new Node(null, this);
        }
        intermediateNodes = new Node[intermediateNodeDimensions.length][];
        for (int i = 0; i < intermediateNodes.length; i++) {
            intermediateNodes[i] = new Node[intermediateNodeDimensions[i]];
        }

        ToDoubleTriIntFunction strengthsSource;
        if (brainToInheritFrom == null) strengthsSource = (x, y, z) -> Utils.randomNeg1To1();
        else {
            strengthsSource = (x, y, z) -> getMutatedConnectionStrengthFromBrain(brainToInheritFrom, x, y, z);
            mutability = brainToInheritFrom.mutability + Utils.randomNeg1To1() * (brainToInheritFrom.mutability / 2); // don't let an individual decide to permanently shut off evolution
        }

        for (int i = 0; i < intermediateNodes.length + 1; i++) {
            Node[] previousLayer;
            Node[] currentLayer;
            if (i == 0) previousLayer = inputNodes;
            else previousLayer = intermediateNodes[i - 1];

            if (i == intermediateNodes.length) currentLayer = outputNodes;
            else currentLayer = intermediateNodes[i];

            for (int j = 0; j < currentLayer.length; j++) {
                Connection[] connections = new Connection[previousLayer.length];
                for (int k = 0; k < connections.length; k++) {
                    connections[k] = new Connection(strengthsSource.apply(i, j, k), previousLayer[k]);
                }
                currentLayer[j] = new Node(connections, this);
            }
        }
    }

    public double[] getOutputs (double[] inputs) {
        for (int i = 0; i < inputNodeCount; i++) {
            inputNodes[i].setInput(inputs[i]);
        }
        double[] outputs = new double[outputNodeCount];
        for (int i = 0; i < outputNodeCount; i++) {
            outputs[i] = outputNodes[i].getOutput();
        }
        currentCycle ^= true;
        return outputs;
    }

    private double getMutatedConnectionStrengthFromBrain (Brain brainToInheritFrom, int layerAfterInput, int layerMember, int connection) {
        double mutatedStrength = brainToInheritFrom.getConnectionStrength(layerAfterInput, layerMember, connection) + (Utils.randomNeg1To1() * mutability);
        mutatedStrength = Math.min(mutatedStrength, 1);
        return Math.max(mutatedStrength, -1);
    }

    private double getConnectionStrength (int layerAfterInput, int layerMember, int connection) {
        Node[] layerToRetrieveFrom;
        if (layerAfterInput == intermediateNodes.length) layerToRetrieveFrom = outputNodes;
        else layerToRetrieveFrom = intermediateNodes[layerAfterInput];
        return layerToRetrieveFrom[layerMember].getConnectionStrength(connection);
    }

    public boolean getCurrentCycle () {
        return currentCycle;
    }

    public void draw (Graphics g) {
        int layerCount = intermediateNodeDimensions.length + 2;
        double layerDivision = panelWidth / (layerCount + 1);  // + 1 is that we won't place nodes on the edge
        for (int i = 0; i < layerCount; i++) {
            Node[] nodes;
            if (i == 0) nodes = inputNodes;
            else if (i == layerCount - 1) nodes = outputNodes;
            else nodes = intermediateNodes[i - 1];

            double nodeDivision = panelHeight / (nodes.length + 1);
            if (nodes != outputNodes) {
                Node[] nextNodes;
                if (i == intermediateNodeDimensions.length) nextNodes = outputNodes;
                else nextNodes = intermediateNodes[i];

                double nextNodeDivision = panelHeight / (nextNodes.length + 1);
                for (int j = 0; j < nodes.length; j++) {
                    for (int k = 0; k < nextNodes.length; k++) {
                        g.setColor(nextNodes[k].getConnectionColor(j));
                        g.drawLine((int)((i + 1) * layerDivision), (int)((j + 1) * nodeDivision), (int)((i + 2) * layerDivision), (int)((k + 1) * nextNodeDivision));
                    }
                }
            }
            for (int j = 0; j < nodes.length; j++) {
                g.setColor(nodes[j].getColor());
                g.fillOval((int)((i + 1) * layerDivision) - radius, (int)((j + 1) * nodeDivision) - radius, diameter, diameter);
            }
        }
    }
}
