package net.snbeast.javaTrackRNN;

import java.awt.*;

public class Racer implements Comparable<Racer> {
    private static final double maxVelocity = 5;
    private static final double fixedAcceleration = 1;
    private static final double raycastResolution = 10;
    private static final double raycastAngleOffset = 0.5;
    public static final int apothem = 5;
    public static final int doubleApothem = apothem * 2;
    private static final int initialLimit = 300;
    private static final int maxClock = 900;

    private final Brain brain;
    private final Map map;
    private final double mapHypot;
    private double centerX;
    private double centerY;
    private double velocityMagnitude = 0;
    private double velocityDirection;
    private double accelerationDirection;
    private double accelerationDirectionVelocity = 0;

    private double velocityX = 0;
    private double velocityY = 0;

    private double[][] previousRaycasts = new double[3][];
    private double[] previousBrainOutput = new double[Brain.outputNodeCount];

    private boolean dead = false;
    private boolean touchingCheckpointLastFrame = false;
    private long score = 0;
    private int clock = 0;
    private int clockMax = initialLimit;
    private boolean preventImmortality = true;

    public Racer (Brain brain, Map map) {
        this.brain = brain;
        this.map = map;
        mapHypot = Math.hypot(map.getImage().getWidth(), map.getImage().getHeight());
        centerX = map.getInitialX();
        centerY = map.getInitialY();
        previousRaycasts[0] = new double[]{centerX, centerY};
        previousRaycasts[1] = new double[]{centerX, centerY};
        previousRaycasts[2] = new double[]{centerX, centerY};
        velocityDirection = map.getInitialDirection();
        accelerationDirection = velocityDirection;
    }
    public void move () {
        if (!dead) {
            clock++;
            checkDead();
            if (dead) return;
            checkCheckpoints();
            accelerationDirection += accelerationDirectionVelocity;
            double accelerationX = fixedAcceleration * Math.cos(accelerationDirection);
            double accelerationY = fixedAcceleration * Math.sin(accelerationDirection);
            double sumX = velocityX + accelerationX;
            double sumY = velocityY + accelerationY;
            velocityMagnitude = Math.min(Math.hypot(sumX, sumY), maxVelocity);
            velocityDirection = Math.atan2(sumY, sumX);
            velocityX = velocityMagnitude * Math.cos(velocityDirection);
            velocityY = velocityMagnitude * Math.sin(velocityDirection);
            centerX += velocityX;
            centerY += velocityY;
        }
    }
    public void checkDead () {
        if (clock >= clockMax) {
            dead = true;
            return;
        }

        int boundLeft = (int)centerX - apothem;
        int boundRight = boundLeft + doubleApothem;
        int boundUp = (int)centerY - apothem;
        int boundDown = boundUp + doubleApothem;
        for (int x = boundLeft; x < boundRight; x++) {
            for (int y = boundUp; y < boundDown; y++) {
                if (new Color(map.getImage().getRGB(x, y), true).equals(map.getOobColor())) {
                    dead = true;
                    return;
                }
            }
        }
    }
    public void checkCheckpoints () {
        Checkpoint[] checkpoints = map.getCheckpoints();
        Checkpoint currentCheckpoint = checkpoints[Math.floorMod(score, checkpoints.length)];
        Checkpoint nextCheckpoint = checkpoints[Math.floorMod(score + 1, checkpoints.length)];
        if (touchingCheckpointLastFrame) {
            touchingCheckpointLastFrame = collisionWithCheckpoint(currentCheckpoint) || collisionWithCheckpoint(nextCheckpoint);
        }
        else {
            if (collisionWithCheckpoint(currentCheckpoint)) {
                score--;
                touchingCheckpointLastFrame = true;

                if (score == 0) {
                    preventImmortality = true;
                }
            }
            else if (collisionWithCheckpoint(nextCheckpoint)) {
                score++;
                touchingCheckpointLastFrame = true;
                
                if (score % checkpoints.length == 0 && !preventImmortality) {
                    clockMax = maxClock;
                    preventImmortality = true;
                }
                else if (score % checkpoints.length == 1) {
                    preventImmortality = false;
                }
            }
        }
    }
    private boolean collisionWithCheckpoint (Checkpoint c) {
        int cxm = (int)centerX - apothem;
        int cxp = cxm + doubleApothem;
        int cym = (int)centerY - apothem;
        int cyp = cym + doubleApothem;
        return lineWithLineCollision(cxm, cym, cxp, cym, c)
            || lineWithLineCollision(cxm, cyp, cxp, cyp, c)
            || lineWithLineCollision(cxm, cym, cxm, cyp, c)
            || lineWithLineCollision(cxp, cym, cxp, cyp, c);
    }
    private boolean lineWithLineCollision (int x1, int y1, int x2, int y2, Checkpoint c) {
        double tTop = (x1 - c.x1()) * (c.y1() - c.y2()) - (y1 - c.y1()) * (c.x1() - c.x2());
        double uTop = (x1 - c.x1()) * (y1 - y2) - (y1 - c.y1()) * (x1 - x2);
        double bottom = (x1 - x2) * (c.y1() - c.y2()) - (y1 - y2) * (c.x1() - c.x2());
        if (bottom < 0) {
            return tTop >= bottom && tTop <= 0 && uTop >= bottom && uTop <= 0;
        }
        return tTop <= bottom && tTop >= 0 && uTop <= bottom && uTop >= 0;
    }
    public void runBrain () {
        if (!dead) {
            previousRaycasts[0] = castRay(accelerationDirection - raycastAngleOffset);
            previousRaycasts[1] = castRay(accelerationDirection);
            previousRaycasts[2] = castRay(accelerationDirection + raycastAngleOffset);

            double[] inputs = new double[Brain.inputNodeCount];
            inputs[0] = velocityX;
            inputs[1] = velocityY;
            inputs[2] = accelerationDirection;
            inputs[3] = accelerationDirectionVelocity;
            inputs[4] = Math.hypot(previousRaycasts[0][0] - centerX, previousRaycasts[0][1] - centerY) / mapHypot;
            inputs[5] = Math.hypot(previousRaycasts[1][0] - centerX, previousRaycasts[1][1] - centerY) / mapHypot;
            inputs[6] = Math.hypot(previousRaycasts[2][0] - centerX, previousRaycasts[2][1] - centerY) / mapHypot;
            inputs[7] = previousBrainOutput[Brain.outputNodeCount - 1]; // memory
            inputs[8] = 1; // constant

            for (int i = 0; i < 4; i++) {   // don't touch anything that's already normalized
                inputs[i] = Utils.sigmoid(inputs[i]);
            }

            previousBrainOutput = brain.getOutputs(inputs);
            accelerationDirectionVelocity = previousBrainOutput[0];
        }
    }
    private double[] castRay (double direction) {
        double raycastDX = raycastResolution * Math.cos(direction);
        double raycastDY = raycastResolution * Math.sin(direction);
        double raycastX = centerX + raycastDX;
        double raycastY = centerY + raycastDY;
        while (!new Color(map.getImage().getRGB((int)raycastX, (int)raycastY), true).equals(map.getOobColor())) {
            raycastX += raycastDX;
            raycastY += raycastDY;
        }
        return new double[]{raycastX, raycastY};
    }
    public void draw (Graphics g) {
        if (!dead) {
            g.fillRect((int)centerX - apothem, (int)centerY - apothem, doubleApothem, doubleApothem);
            if (JavaTrackRNN.DEBUG && JavaTrackRNN.DEBUG_AI) {
                g.drawLine((int)centerX, (int)centerY, (int)previousRaycasts[0][0], (int)previousRaycasts[0][1]);
                g.drawLine((int)centerX, (int)centerY, (int)previousRaycasts[1][0], (int)previousRaycasts[1][1]);
                g.drawLine((int)centerX, (int)centerY, (int)previousRaycasts[2][0], (int)previousRaycasts[2][1]);
            }
        }
    }

    public Brain getBrain () {
        return brain;
    }

    public long getScore () {
        return score;
    }

    public void debugChangeX (double change) {
        if (JavaTrackRNN.DEBUG) {
            centerX += change;
        }
        else {
            Utils.errorTrace("debugChangeX called outside debug mode");
        }
    }

    public void debugChangeY (double change) {
        if (JavaTrackRNN.DEBUG) {
            centerY += change;
        }
        else {
            Utils.errorTrace("debugChangeY called outside debug mode");
        }
    }

    public int compareTo (Racer o) {
        return ((Long)score).compareTo(o.getScore());
    }

    public boolean getDead () {
        return dead;
    }
}
