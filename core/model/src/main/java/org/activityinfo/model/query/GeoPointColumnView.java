package org.activityinfo.model.query;

import org.activityinfo.model.type.geo.Extents;

/**
 * Created by yuriyz on 9/13/2016.
 */
public class GeoPointColumnView implements GeoColumn {

    /**
     * Stores the latitude and longitude
     */
    private double[] coordinates;
    private int numRows;

    public GeoPointColumnView() {
    }

    /**
     * @param coordinates an array of the coordinates of latitude and longitude
     */
    public GeoPointColumnView(double[] coordinates) {
        this.coordinates = coordinates;
        this.numRows = coordinates.length / 2;
    }


    @Override
    public ColumnType getType() {
        return ColumnType.GEOGRAPHIC_POINT;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    public int numCoordinates() {
        return coordinates.length;
    }

    @Override
    public Object get(int row) {
        return getExtents(row);
    }

    @Override
    public double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public Extents getExtents(int row) {
        int offset = row * 2;
        return Extents.fromLatLng(offset, offset + 1);
    }

    public double getCoordinate(int coordinateIndex) {
        return coordinates[coordinateIndex];
    }

    @Override
    public int getBoolean(int row) {
        return -1;
    }


    @Override
    public String toString() {
        return "[ GeoPointColumnView " + numRows + " rows]";
    }
}
