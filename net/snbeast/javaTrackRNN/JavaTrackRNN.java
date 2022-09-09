package net.snbeast.javaTrackRNN;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class JavaTrackRNN extends JPanel implements ActionListener, KeyListener {
    public static boolean DEBUG = false;
    public static boolean DEBUG_AI = false;

    private final JFrame mainFrame = new JFrame("JavaTrackRNN");
    private final Container mainCanvas = mainFrame.getContentPane();
    private final JFrame brainFrame = new JFrame("Best Brain");
    private final Container brainCanvas = brainFrame.getContentPane();
    private final Map map;
    private Racer racer = null;
    private BrainWindow bw = null;
    private Generation generation = null;
    private Timer t = new Timer(50/3, this);

    // debug only
    private boolean downPressed = false;
    private boolean upPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean shiftPressed = false;

    private JavaTrackRNN (Map map, Brain seedBrain) {
        this.map = map;
        if (DEBUG) {
            racer = new Racer(seedBrain == null ? new Brain(seedBrain) : seedBrain, map);
            bw = new BrainWindow(racer.getBrain());
            brainCanvas.add(bw);
            brainFrame.addKeyListener(this);
            brainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            brainFrame.setResizable(false);
            brainFrame.setLocationRelativeTo(null);
            brainFrame.pack();
            brainFrame.setVisible(true);
        }
        else {
            generation = new Generation(seedBrain, map);
        }

        setPreferredSize(new Dimension(map.getImage().getWidth(), map.getImage().getHeight()));
        mainCanvas.add(this);
        mainFrame.addKeyListener(this);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setVisible(true);

        t.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(map.getImage(), 0, 0, null);
        if (DEBUG) {
            g.setColor(Color.RED);
            for (Checkpoint c : map.getCheckpoints()) {
                g.drawLine(c.x1(), c.y1(), c.x2(), c.y2());
            }
            g.setColor(Color.BLUE);
            racer.draw(g);
            String score = Long.toString(racer.getScore());
            g.drawChars(score.toCharArray(), 0, score.length(), 300, 300);
        }
        else {
            g.setColor(Color.BLUE);
            generation.draw(g);
        }
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (DEBUG) {
            racer.checkDead();
            racer.checkCheckpoints();
            if (DEBUG_AI) {
                racer.runBrain();
                racer.move();
                bw.repaint();
            }
            else {
                if (!racer.getDead()) {
                    double positionIncrement = shiftPressed ? 5 : 1;
                    if (upPressed && !downPressed) racer.debugChangeY(-positionIncrement);
                    else if (downPressed && !upPressed) racer.debugChangeY(positionIncrement);
                    if (leftPressed && !rightPressed) racer.debugChangeX(-positionIncrement);
                    else if (rightPressed && !leftPressed) racer.debugChangeX(positionIncrement);
                }
            }
        }
        else {
            generation.runBrains();
            generation.move();
            if (generation.allDead()) {
                generation = new Generation(generation, map);
            }
        }
        repaint();
    }

    @Override
    public void keyTyped (KeyEvent e) {
        if (DEBUG) {
            if (e.getKeyChar() == 'r') {
                racer = new Racer(new Brain(null), map);
                brainCanvas.remove(bw);
                brainCanvas.revalidate();
                bw = new BrainWindow(racer.getBrain());
                brainCanvas.add(bw);
            }
        }
        else {
            if (e.getKeyChar() == 'd') {
                Racer best = generation.getBest();
                File dumpFile = new File("dump.brain");
                if (dumpFile.exists()) {
                    JOptionPane.showMessageDialog(null, "\"dump.brain\" already exists.");
                }
                else {
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dumpFile))) {
                        oos.writeObject(best.getBrain());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "error while writing \"dump.brain\"");
                        System.exit(-1);
                    }
                }
            }
        }
    }
    @Override
    public void keyPressed (KeyEvent e) {
        if (DEBUG) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    upPressed = true;
                    break;
                case KeyEvent.VK_DOWN:
                    downPressed = true;
                    break;
                case KeyEvent.VK_LEFT:
                    leftPressed = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    rightPressed = true;
                    break;
                case KeyEvent.VK_SHIFT:
                    shiftPressed = true;
                    break;
            }
        }
    }
    @Override
    public void keyReleased (KeyEvent e) {
        if (DEBUG) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    upPressed = false;
                    break;
                case KeyEvent.VK_DOWN:
                    downPressed = false;
                    break;
                case KeyEvent.VK_LEFT:
                    leftPressed = false;
                    break;
                case KeyEvent.VK_RIGHT:
                    rightPressed = false;
                    break;
                case KeyEvent.VK_SHIFT:
                    shiftPressed = false;
                    break;
            }
        }
    }

    public static void main (String[] args) throws FileNotFoundException {
        String brainDump = "";
        String mapPack = "";
        for (String st : args) {
            if (st.equals("-?") || st.equals("-h") || st.equals("--help")) {
                System.out.println("Usage: JavaTrackRNN [--braindump=brainfile] [--mappack=pack.zip]");
                System.out.println("If there is no map pack specified, \"default.zip\" will be used.");
                System.out.println("Press \"d\" while running to make a brain dump of the best player.");
                System.exit(0);
            }
            if (st.startsWith("--braindump=")) {
                brainDump = st.replaceFirst("--braindump=", "");
            }
            if (st.startsWith("--mappack=")) {
                mapPack = st.replaceFirst("--mappack=", "");
            }
        }
        Map mapArg;
        if (!mapPack.isEmpty()) mapArg = new Map(mapPack);
        else mapArg = new Map();

        Brain brainArg = null;
        if (!brainDump.isEmpty()) brainArg = Brain.readBrainFromFile(brainDump);

        new JavaTrackRNN(mapArg, brainArg);
    }
}
