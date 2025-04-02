package nufo.diss;

public class Statistics {
    private int count = 0;
    private double sum = 0;
    private double sumOfSquares = 0.0;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    public void addValue(double value) {
        count++;
        sum += value;
        sumOfSquares += value * value;

        if (value < min) {
            min = value;
        }

        if (value > max) {
            max = value;
        }
    }

    public int getCount() {
        return count;
    }

    public double getSum() {
        return sum;
    }

    public double getMin() {
        return count > 0 ? min : Double.NaN;
    }

    public double getMax() {
        return count > 0 ? max : Double.NaN;
    }

    public double getMean() {
        return count > 0 ? sum / count : Double.NaN;
    }

    public double getStandardDeviation() {
        return count > 1 ? Math.sqrt((sumOfSquares / count) - Math.pow(sum / count, 2)) : Double.NaN;
    }

    public double getSampleStandardDeviation() {
        return count > 1 ? Math.sqrt((sumOfSquares - (Math.pow(sum, 2) / count)) / (count - 1)) : Double.NaN;
    }

    public double getConfidenceIntervalLowerBound() {
        return getMean() - getConfidenceIntervalHalfWidth();
    }

    public double getConfidenceIntervalUpperBound() {
        return getMean() + getConfidenceIntervalHalfWidth();
    }

    private double getConfidenceIntervalHalfWidth() {
        return count >= 30 ? (getSampleStandardDeviation() * 1.96) / Math.sqrt(count) : Double.NaN;
    }

    public void reset() {
        count = 0;
        sum = 0;
        sumOfSquares = 0.0;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
    }

    public Data toData() {
        return new Data(
                getMean(),
                getConfidenceIntervalLowerBound(),
                getConfidenceIntervalUpperBound()
        );
    }

    public record Data(
            double mean,
            double confidenceIntervalLowerBound,
            double confidenceIntervalUpperBound
    ) {}
}
