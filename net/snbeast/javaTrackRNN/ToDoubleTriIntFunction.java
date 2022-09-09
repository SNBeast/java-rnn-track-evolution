package net.snbeast.javaTrackRNN;

@FunctionalInterface
public interface ToDoubleTriIntFunction {
    double apply(int x, int y, int z);
}
