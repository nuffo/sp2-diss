package nufo.diss.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmpiricalGenerator extends Generator {
    protected final List<Double> minValues;
    protected final List<Double> maxValues;
    private final List<Double> probabilities;
    protected final List<Double> cumulativeProbabilities;
    protected final List<UniformGenerator> rands;
    protected final Random randProbability;

    public EmpiricalGenerator(List<Double> minValues, List<Double> maxValues, List<Double> probabilities, Mode mode) {
        super(mode);

        if (minValues.size() != maxValues.size() || minValues.size() != probabilities.size()) {
            throw new IllegalArgumentException("Sizes of lists minValues, maxValues and probabilities must be same.");
        }

        this.minValues = minValues;
        this.maxValues = maxValues;

        this.probabilities = probabilities;
        validateProbabilities();

        this.cumulativeProbabilities = new ArrayList<>();
        computeCumulativeProbabilities();

        randProbability = new Random(SeedGenerator.getInstance().nextInt());
        rands = new ArrayList<>();
        for (int i = 0; i < probabilities.size(); i++) {
            rands.add(new UniformGenerator(minValues.get(i), maxValues.get(i), mode));
        }
    }

    @Override
    public int nextInt() {
        return rands.get(getIntervalIndex()).nextInt();
    }

    @Override
    public double nextDouble() {
        return rands.get(getIntervalIndex()).nextDouble();
    }

    private int getIntervalIndex() {
        double probability = randProbability.nextDouble();

        for (int i = 0; i < cumulativeProbabilities.size(); i++) {
            if (probability <= cumulativeProbabilities.get(i)) {
                return i;
            }
        }

        throw new IllegalStateException("No interval found for the probability: " + probability);
    }

    private void computeCumulativeProbabilities() {
        double sum = 0;
        for (double p : probabilities) {
            sum += p;
            cumulativeProbabilities.add(sum);
        }
    }

    private void validateProbabilities() {
        double sum = probabilities.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 1.0) > 1e-9) {
            throw new IllegalArgumentException("Sum of probabilities must be 1.0 but is " + sum);
        }
    }
}
