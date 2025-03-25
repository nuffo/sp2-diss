package nufo.diss;

public class Statistics {
    private int count = 0;
    private double sum = 0;
    private double sumOfSquares = 0.0;
    private double mean = 0.0;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    public void addValue(double value) {
        count++;
        sum += value;
        sumOfSquares += value * value;
        mean = sum / count;

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
        return count > 0 ? mean : Double.NaN;
    }

    public double getStandardDeviation() {
        return count > 1 ? Math.sqrt((sumOfSquares / count) - (mean * mean)) : Double.NaN;
    }


}
