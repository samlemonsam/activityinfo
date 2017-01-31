package org.activityinfo.ui.client.analysis.model;

/**
 * An individual value
 */
public class Point {

    private double value;
    private final String[] dimensions;


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
}
