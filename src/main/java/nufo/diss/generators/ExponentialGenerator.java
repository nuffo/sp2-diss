package nufo.diss.generators;

import java.util.Random;

public class ExponentialGenerator extends Generator {
    private final double lambda;
    private final Random rand;

    public ExponentialGenerator(double lambda) {
        super(Mode.CONTINUOUS);

        this.lambda = lambda;
        this.rand = new Random(SeedGenerator.getInstance().nextInt());
    }

    @Override
    public int nextInt() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public double nextDouble() {
        return -Math.log(1 - rand.nextDouble()) / lambda;
    }
}
