package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;


public class CoordinateReader implements DoubleReader {

    public CoordinateReader(Axis axis) {
        this.axis = axis;
    }


    public CoordinateReader(SymbolExpr field) {
        if (field.getName().equals("longitude")) {
            this.axis = Axis.LONGITUDE;
        } else if (field.getName().equals("latitude")) {
            this.axis = Axis.LATITUDE;
        } else {
            throw new IllegalArgumentException("field: " + field.getName());
        }
    }

    public enum Axis {
        LATITUDE,
        LONGITUDE
    }
    
    private final Axis axis;

    @Override
    public double read(FieldValue value) {
        if(value instanceof GeoPoint) {
            GeoPoint point = (GeoPoint) value;
            switch (axis) {
                case LATITUDE:
                    return point.getLatitude();
                case LONGITUDE:
                    return point.getLongitude();
                default:
                    throw new IllegalStateException();
            }
        }
        return Double.NaN;
    }


}
