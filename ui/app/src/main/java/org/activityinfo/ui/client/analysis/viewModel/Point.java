package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.base.Joiner;

import java.util.Arrays;

/**
 * An individual value
 */
public class Point {

    private double value;

    private final String[] dimensions;

    public Point(String[] dimensions, double value) {
        this.dimensions = dimensions;
        this.value = value;
    }

    public Point(DimensionSet dimensions) {
        this.dimensions = new String[dimensions.getCount()];
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setDimension(int index, String label) {
        dimensions[index] = label;
    }


    public String getDimension(int dimensionIndex) {
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
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
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
