package net.snbeast.javaTrackRNN;

import java.util.Arrays;

public class Utils {
    public static double sigmoid (double input) {
        return ((1/(1 + Math.exp(-input))) - 0.5) * 2;
    }
    public static double randomNeg1To1 () {
        return (Math.random() - 0.5) * 2;
    }
    public static void errorTrace (String message) {
        System.err.println(message);
        System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
    }
}
