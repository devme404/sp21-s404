package byow.Core;

import java.util.Random;

public class RandomGen {
    private Random random;
    private static final int ROOM_MIN_SIZE = 3;
    private static final int HALLWAY_MIN_WIDTH = 1, HALLWAY_MAX_WIDTH = 3;

    RandomGen(long seed) {
        random = new Random(seed);
    }

    public int intTo(int max) {
        return RandomUtils.uniform(random, max);
    }

    public int roomSize(int max) {
        return RandomUtils.uniform(random, ROOM_MIN_SIZE, max);
    }

    public int numberTo(int size, int ratio) {
        return RandomUtils.poisson(random, size / ratio) + 1;
    }

    public int sampleEdge(int min, int max) {
        if (max - min < 4) {
            return min + 1;
        }
        return RandomUtils.uniform(random, min + 1, max - 2);
    }

    public int hallwayWidth() {
        return RandomUtils.uniform(random, HALLWAY_MIN_WIDTH, HALLWAY_MAX_WIDTH);
    }

}
