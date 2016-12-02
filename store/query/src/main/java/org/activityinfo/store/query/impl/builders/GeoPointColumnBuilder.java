package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.GeoPointColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.query.impl.PendingSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuriyz on 9/13/2016.
 */
public class GeoPointColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;

    private List<Double> coordinates = new ArrayList<>();

    public GeoPointColumnBuilder(PendingSlot<ColumnView> result) {
        this.result = result;
    }

    @Override
    public void onNext(FieldValue value) {
        if (value instanceof GeoPoint) {
            GeoPoint point = ((GeoPoint) value);
            coordinates.add(point.getLatitude());
            coordinates.add(point.getLongitude());
        } else {
            coordinates.add(Double.NaN);
            coordinates.add(Double.NaN);
        }
    }

    @Override
    public void done() {
        double[] array = new double[coordinates.size()];
        for (int i = 0; i < coordinates.size(); ++i) {
            array[i] = coordinates.get(i);
        }
        result.set(new GeoPointColumnView(array));
    }
}