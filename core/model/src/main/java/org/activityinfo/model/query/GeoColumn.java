package org.activityinfo.model.query;

/**
 * Created by yuriyz on 9/13/2016.
 */
public interface GeoColumn extends ColumnView {

    int numCoordinates();

    double getCoordinate(int index);
}
