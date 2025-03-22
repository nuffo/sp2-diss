package nufo.diss.generators;

import java.util.Random;

public class SeedGenerator extends Generator {
    private final Random rand;

    private static volatile SeedGenerator instance;

    private SeedGenerator() {
        super(Mode.DISCRETE);

        this.rand = new Random();
    }

    public static SeedGenerator getInstance() {
        if (instance == null) {
            synchronized (SeedGenerator.class) {
                if (instance == null) {
                    instance = new SeedGenerator();
                }
            }
        }
        return instance;
    }

    @Override
    public int nextInt() {
        return rand.nextInt();
    }

    @Override
    public double nextDouble() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
