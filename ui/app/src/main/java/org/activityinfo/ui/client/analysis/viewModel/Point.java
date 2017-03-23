package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.ui.client.analysis.model.Statistic;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * An individual value
 */
public class Point {

    private final double value;

    @Nonnull
    private final Statistic statistic;

    @Nonnull
    private final String[] dimensions;

    public Point(double value, String[] dimensions) {
        this.value = value;
        this.statistic = Statistic.SUM;
        this.dimensions = dimensions;
    }

    public Point(double value, Statistic statistic, String[] dimensions) {
        this.value = value;
        this.statistic = statistic;
        this.dimensions = dimensions;
    }

    public Statistic getStatistic() {
        return statistic;
    }

    public double getValue() {
        return value;
    }

    public String getCategory(int dimensionIndex) {
        return dimensions[dimensionIndex];
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Point{");
        s.append(value);
        s.append(',');
        s.append(statistic.name());
        for (int i = 0; i < dimensions.length; i++) {
            s.append(",");
            if(dimensions[i] == null) {
                s.append("<NA>");
            } else {
                s.append(dimensions[i]);
            }
        }
        s.append("}");
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (Double.compare(point.value, value) != 0) return false;
        if (statistic != point.statistic) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(dimensions, point.dimensions);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + statistic.hashCode();
        result = 31 * result + Arrays.hashCode(dimensions);
        return result;
    }
}
