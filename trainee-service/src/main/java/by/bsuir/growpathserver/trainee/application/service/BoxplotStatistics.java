package by.bsuir.growpathserver.trainee.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BoxplotStatistics {

    private final double min;
    private final double q1;
    private final double median;
    private final double q3;
    private final double max;
    private final List<Double> outliers;
    private final int sampleSize;

    private BoxplotStatistics(
            double min,
            double q1,
            double median,
            double q3,
            double max,
            List<Double> outliers,
            int sampleSize) {
        this.min = min;
        this.q1 = q1;
        this.median = median;
        this.q3 = q3;
        this.max = max;
        this.outliers = List.copyOf(outliers);
        this.sampleSize = sampleSize;
    }

    public static BoxplotStatistics empty() {
        return new BoxplotStatistics(0, 0, 0, 0, 0, List.of(), 0);
    }

    public static BoxplotStatistics fromValues(List<Double> values) {
        if (Objects.isNull(values) || values.isEmpty()) {
            return empty();
        }
        List<Double> sorted = values.stream()
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        if (sorted.isEmpty()) {
            return empty();
        }

        double q1 = percentile(sorted, 0.25);
        double median = percentile(sorted, 0.5);
        double q3 = percentile(sorted, 0.75);
        double iqr = q3 - q1;
        double lowerFence = q1 - 1.5 * iqr;
        double upperFence = q3 + 1.5 * iqr;

        List<Double> outliers = new ArrayList<>();
        List<Double> whiskerValues = new ArrayList<>();
        for (double value : sorted) {
            if (value < lowerFence || value > upperFence) {
                outliers.add(value);
            }
            else {
                whiskerValues.add(value);
            }
        }

        double min;
        double max;
        if (whiskerValues.isEmpty()) {
            min = sorted.getFirst();
            max = sorted.getLast();
        }
        else {
            min = whiskerValues.getFirst();
            max = whiskerValues.getLast();
        }

        return new BoxplotStatistics(min, q1, median, q3, max, outliers, sorted.size());
    }

    private static double percentile(List<Double> sorted, double percentile) {
        if (sorted.size() == 1) {
            return sorted.getFirst();
        }
        double index = percentile * (sorted.size() - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        if (lowerIndex == upperIndex) {
            return sorted.get(lowerIndex);
        }
        double weight = index - lowerIndex;
        return sorted.get(lowerIndex) * (1.0 - weight) + sorted.get(upperIndex) * weight;
    }

    public double getMin() {
        return min;
    }

    public double getQ1() {
        return q1;
    }

    public double getMedian() {
        return median;
    }

    public double getQ3() {
        return q3;
    }

    public double getMax() {
        return max;
    }

    public List<Double> getOutliers() {
        return outliers;
    }

    public int getSampleSize() {
        return sampleSize;
    }
}
