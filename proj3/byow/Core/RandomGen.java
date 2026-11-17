package byow.Core;

import java.util.Random;

public class RandomGen {
    private Random random;

    RandomGen(long seed) {
        random = new Random(seed);
    }

    public int intTo(int max) {
        return RandomUtils.uniform(random, max);
    }

    public int intInterval(int min, int max) {
        return RandomUtils.uniform(random, min, max);
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

}
