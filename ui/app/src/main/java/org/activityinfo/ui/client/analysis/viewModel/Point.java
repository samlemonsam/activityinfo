package org.activityinfo.ui.client.analysis.viewModel;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * An individual value
 */
public class Point {

    public static final String TOTAL = "\0\0TOTAL";

    public static final String MISSING = "\0\0TOTAL";

    private final double value;

    private final String formattedValue;

    @Nonnull
    private final String[] dimensions;

    public Point(double value, String formattedValue, String[] dimensions) {
        assert !anyNull(dimensions) : "Dimension categories cannot be null";
        this.value = value;
        this.formattedValue = formattedValue;
        this.dimensions = dimensions;
    }

    private boolean anyNull(String[] dimensions) {
        for (String dimension : dimensions) {
            if(dimension == null) {
                return true;
            }
        }
        return false;
    }

    public double getValue() {
        return value;
    }

    public String getFormattedValue() {
        return formattedValue;
    }

    public String getCategory(int dimensionIndex) {
        return dimensions[dimensionIndex];
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Point{");
        s.append(value);
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
        return Arrays.equals(dimensions, point.dimensions);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(dimensions);
        return result;
    }
}
