package net.snbeast.javaTrackRNN;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

import javax.imageio.*;

public class Map {
    private BufferedImage imageData = null;
    private Color oobColor = null;
    private Checkpoint[] checkpoints = null;
    private double initialDirection = Double.POSITIVE_INFINITY;
    private int initialX = -1;
    private int initialY = -1;

    public Map () {
        this(new ZipInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("default.zip")));
    }

    public Map (String s) throws FileNotFoundException {
        this(new ZipInputStream(new FileInputStream(s)));
    }

    private Map (ZipInputStream in) {
        try {
            ZipEntry ze;
            while ((ze = in.getNextEntry()) != null) {
                if (ze.getName().equals("map.png")) {
                    imageData = ImageIO.read(in);
                }
                else if (ze.getName().equals("checkpoints.txt")) {
                    List<String> checkpointList = processLines(new BufferedReader(new InputStreamReader(in)));
                    checkpoints = readCheckpointCoords(checkpointList);
                }
                else if (ze.getName().equals("initialPlacement.txt")) {
                    List<String> placementContents = processLines(new BufferedReader(new InputStreamReader(in)));
                    String[] initialXY = placementContents.get(0).split(" ");
                    initialX = Integer.parseInt(initialXY[0]);
                    initialY = Integer.parseInt(initialXY[1]);
                    initialDirection = Double.parseDouble(placementContents.get(1));
                }
                else if (ze.getName().equals("oobColor.txt")) {
                    List<String> colorContents = processLines(new BufferedReader(new InputStreamReader(in)));
                    String[] colorNumbers = colorContents.get(0).split(" ");
                    oobColor = new Color(Integer.parseInt(colorNumbers[0]), Integer.parseInt(colorNumbers[1]), Integer.parseInt(colorNumbers[2]), Integer.parseInt(colorNumbers[3]));
                }
                in.closeEntry();
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private List<String> processLines (BufferedReader br) {
        List<String> retValue = new ArrayList<String>();
        try {
            while (br.ready()) {
                String s = br.readLine().split("#")[0].trim();
                if (!s.isEmpty()) retValue.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return retValue;
    }

    private Checkpoint[] readCheckpointCoords (List<String> checkpointList) {
        Checkpoint[] retValue = new Checkpoint[checkpointList.size()];
        for (int i = 0; i < retValue.length; i++) {
            String[] numbers = checkpointList.get(i).split(" ");
            retValue[i] = new Checkpoint(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), Integer.parseInt(numbers[2]), Integer.parseInt(numbers[3]));
        }
        return retValue;
    }

    public BufferedImage getImage () {
        return imageData;
    }
    public Color getOobColor () {
        return oobColor;
    }
    public Checkpoint[] getCheckpoints () {
        return checkpoints;
    }
    public double getInitialDirection () {
        return initialDirection;
    }
    public int getInitialX () {
        return initialX;
    }
    public int getInitialY () {
        return initialY;
    }
}
