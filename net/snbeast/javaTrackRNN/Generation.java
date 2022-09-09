package net.snbeast.javaTrackRNN;

import java.awt.*;
import java.util.*;

public class Generation {
    public static final int generationSize = 1000;
    private Racer[] members = new Racer[generationSize];

    public Generation (Brain seedBrain, Map map) {
        int i = 0;
        if (seedBrain != null) {
            members[0] = new Racer(seedBrain, map);
            i++;
        }
        for (; i < generationSize; i++) {
            members[i] = new Racer(new Brain(seedBrain), map);
        }
    }
    public Generation (Generation previousGeneration, Map map) {
        Arrays.sort(previousGeneration.members);
        for (int i = 0; i < generationSize; i += 2) {
            members[i] = new Racer(previousGeneration.members[previousGeneration.members.length - ((i >> 1) + 1)].getBrain(), map);
            members[i + 1] = new Racer(new Brain(previousGeneration.members[previousGeneration.members.length - ((i >> 1) + 1)].getBrain()), map);
        }
    }

    public void move () {
        for (Racer r : members) {
            r.move();
        }
    }
    public void runBrains () {
        for (Racer r : members) {
            r.runBrain();
        }
    }
    public void draw (Graphics g) {
        for (Racer r : members) {
            r.draw(g);
        }
    }

    public boolean allDead () {
        for (Racer r : members) {
            if (!r.getDead()) {
                return false;
            }
        }
        return true;
    }
    public Racer getBest () {
        Racer best = members[0];
        for (int i = 1; i < members.length; i++) {
            int compare = members[i].compareTo(best);
            if (compare > 0) best = members[i];
            else if (compare == 0 && !members[i].getDead() && best.getDead()) best = members[i];
        }
        return best;
    }
}
