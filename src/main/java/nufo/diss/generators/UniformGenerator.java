package nufo.diss.generators;

import java.util.Random;

public class UniformGenerator extends Generator {
    private final Random rand;
    private final double min;
    private final double max;

    public UniformGenerator(double min, double max, Mode mode) {
        super(mode);

        this.min = min;
        this.max = max;
        rand = new Random(SeedGenerator.getInstance().nextInt());
    }

    @Override
    public int nextInt() {
        return (int) (rand.nextInt((int) (max - min)) + min);
    }

    @Override
    public double nextDouble() {
        return rand.nextDouble() * (max - min) + min;
    }
}
