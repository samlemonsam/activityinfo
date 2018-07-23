package org.activityinfo.store.query.shared;

import com.google.common.base.Function;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.RecordFieldValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.store.spi.CursorObserver;

class CoordinateReader implements Function<FieldValue, FieldValue> {

    private boolean latitude;

    public CoordinateReader(String component) {
        this.latitude = (component.equalsIgnoreCase("latitude"));
    }

    @Override
    public FieldValue apply(FieldValue input) {
        if(input instanceof GeoPointType) {
            GeoPoint point = (GeoPoint) input;
            if(latitude) {
                return new Quantity(point.getLatitude());
            } else {
                return new Quantity(point.getLongitude());
            }
        } else {
            return null;
        }
    }

}
