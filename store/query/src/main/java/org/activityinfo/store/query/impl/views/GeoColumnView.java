package org.activityinfo.store.query.impl.views;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.geo.Extents;

/**
 * Area column view
 */
public class GeoColumnView implements ColumnView {

    /**
     * Stores the Minimum Bounding Rectangle (MBR) of each row as a block of (x1,y1,x2,y2)
     */
    private final double[] coordinates;
    private final int numRows;

    /**
     * 
     * @param coordinates an array of the coordinates of the minimum bounding rectangles (MBRs) 
     *                    of each row ordered as (x1,y1,x2,y2)
     */
    public GeoColumnView(double[] coordinates) {
        this.coordinates = coordinates;
        this.numRows = coordinates.length / 4;
    }


    @Override
    public ColumnType getType() {
        return ColumnType.GEOGRAPHIC;
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
        int offset = row*4;
        return Extents.create(
                coordinates[offset+0],
                coordinates[offset+1],
                coordinates[offset+2],
                coordinates[offset+3]);
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
        return "[ GeoColumnView " + numRows + " rows]";
    }
}
