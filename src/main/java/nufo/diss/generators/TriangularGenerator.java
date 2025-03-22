package nufo.diss.generators;

import java.util.Random;

public class TriangularGenerator extends Generator {
    private final Random rand;
    private final double min;
    private final double max;
    private final double modus;
    private final double f;

    public TriangularGenerator(double min, double max, double modus) {
        super(Mode.CONTINUOUS);

        rand = new Random(SeedGenerator.getInstance().nextInt());
        this.min = min;
        this.max = max;
        this.modus = modus;
        f = (modus - min) / (max - min);
    }


    @Override
    public int nextInt() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public double nextDouble() {
        double u = rand.nextDouble();

        if (u < f) {
            return min + Math.sqrt(u * (max - min) * (modus - min));
        }
        return max - Math.sqrt((1 - u) * (max - min) * (max - modus));
    }
}
