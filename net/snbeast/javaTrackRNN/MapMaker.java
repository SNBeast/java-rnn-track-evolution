package net.snbeast.javaTrackRNN;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

public class MapMaker extends JPanel implements KeyListener, MouseInputListener {
    private enum MapMakerState {
        OOBColor,
        PositionInit,
        DirectionInit,
        CheckpointFirstPoint,
        CheckpointSecondPoint
    };
    private final JFrame frame = new JFrame("JavaTrackRNN MapMaker");
    private final Container canvas = frame.getContentPane();
    private final BufferedImage image;
    private final File pack;
    private Color oobColor;
    private int startX;
    private int startY;
    private double startDirection;
    private List<Checkpoint> checkpointList = new ArrayList<Checkpoint>();
    private MapMakerState state = MapMakerState.OOBColor;
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private MapMaker (BufferedImage image, File pack) {
        this.image = image;
        this.pack = pack;

        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        canvas.add(this);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        frame.addKeyListener(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        JOptionPane.showMessageDialog(null, "Click to set out of bounds color.");
    }

    private static void usage () {
        System.out.println("Usage: MapMaker --imagefile=image.png --packname=name[.zip]");
        System.exit(0);
    }

    @Override
    public void paintComponent (Graphics g) {
        g.drawImage(image, 0, 0, null);
        g.setColor(Color.BLUE);
        for (Checkpoint c : checkpointList) {
            g.drawLine(c.x1(), c.y1(), c.x2(), c.y2());
        }
        switch (state) {
            case DirectionInit:
            case CheckpointSecondPoint:
                g.drawLine(x1, y1, x2, y2);
                break;
            default:
                break;
        }
        switch (state) {
            case OOBColor:
            case PositionInit:
                break;
            default:
                g.fillRect(startX - Racer.apothem, startY - Racer.apothem, Racer.doubleApothem, Racer.doubleApothem);
                break;
        }
    }

    @Override
    public void mouseReleased (MouseEvent e) {
        switch (state) {
            case OOBColor:
                oobColor = new Color(image.getRGB(e.getX(), e.getY()));
                JOptionPane.showMessageDialog(null, "Click to set spawn position.");
                state = MapMakerState.PositionInit;
                break;
            case PositionInit:
                x1 = x2 = startX = e.getX();
                y1 = y2 = startY = e.getY();
                JOptionPane.showMessageDialog(null, "Click to set spawn angle.");
                state = MapMakerState.DirectionInit;
                repaint();
                break;
            case DirectionInit:
                startDirection = Math.atan2(y2 - y1, x2 - x1);
                JOptionPane.showMessageDialog(null, "Click to set one of the first checkpoint's points. Put it behind spawn.");
                state = MapMakerState.CheckpointFirstPoint;
                repaint();
                break;
            case CheckpointFirstPoint:
                x1 = x2 = e.getX();
                y1 = y2 = e.getY();
                JOptionPane.showMessageDialog(null, "Click to set the checkpoint's second point.");
                state = MapMakerState.CheckpointSecondPoint;
                break;
            case CheckpointSecondPoint:
                checkpointList.add(new Checkpoint(x1, y1, x2, y2));
                JOptionPane.showMessageDialog(null, "Click to set another checkpoint's first point, or type \"f\" if done.");
                state = MapMakerState.CheckpointFirstPoint;
                repaint();
                break;
        }
    }
    @Override
    public void mouseMoved (MouseEvent e) {
        switch (state) {
            case DirectionInit:
            case CheckpointSecondPoint:
                x2 = e.getX();
                y2 = e.getY();
                repaint();
                break;
            default:
                break;
        }
    }
    @Override
    public void mouseDragged (MouseEvent e) {
        mouseMoved(e);
    }
    @Override
    public void keyTyped (KeyEvent e) {
        if (e.getKeyChar() == 'f' && state.equals(MapMakerState.CheckpointFirstPoint) && checkpointList.size() > 0) {
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(pack))) {
                zos.putNextEntry(new ZipEntry("map.png"));
                ImageIO.write(image, "png", zos);
                zos.putNextEntry(new ZipEntry("initialPlacement.txt"));
                zos.write((startX + " " + startY + "\n" + startDirection + "\n").getBytes());
                zos.putNextEntry(new ZipEntry("oobColor.txt"));
                zos.write((oobColor.getRed() + " " + oobColor.getGreen() + " " + oobColor.getBlue() + " " + oobColor.getAlpha() + "\n").getBytes());
                zos.putNextEntry(new ZipEntry("checkpoints.txt"));
                StringBuilder checkpoints = new StringBuilder(checkpointList.size() * 15);
                for (Checkpoint c : checkpointList) {
                    checkpoints.append(c.x1() + " " + c.y1() + " " + c.x2() + " " + c.y2() + "\n");
                }
                zos.write(checkpoints.toString().getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
            System.exit(0);
        }
    }

    @Override
    public void mouseClicked (MouseEvent e) {}
    @Override
    public void mousePressed (MouseEvent e) {}
    @Override
    public void mouseEntered (MouseEvent e) {}
    @Override
    public void mouseExited (MouseEvent e) {}
    @Override
    public void keyPressed (KeyEvent e) {}
    @Override
    public void keyReleased (KeyEvent e) {}

    public static void main (String[] args) throws IOException {
        if (args.length != 2) usage();

        String imageFile = "";
        String packName = "";
        for (String st : args) {
            if (st.startsWith("--imagefile=")) {
                imageFile = st.replaceFirst("--imagefile=", "");
            }
            if (st.startsWith("--packname=")) {
                packName = st.replaceFirst("--packname=", "");
            }
        }
        if (imageFile.isEmpty() || packName.isEmpty()) usage();
        File pack = new File(packName.endsWith(".zip") ? packName : packName + ".zip");
        if (pack.exists()) {
            System.out.println(pack.getName() + " already exists.");
            System.exit(0);
        }
        pack.createNewFile();
        new MapMaker(ImageIO.read(new File(imageFile)), pack);
    }
}
