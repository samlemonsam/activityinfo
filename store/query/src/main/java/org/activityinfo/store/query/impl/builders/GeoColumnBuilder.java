package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoArea;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.store.query.impl.views.GeoColumnView;

import java.util.ArrayList;
import java.util.List;


public class GeoColumnBuilder implements ColumnViewBuilder, CursorObserver<FieldValue> {
    
    private List<Double> coordinates = new ArrayList<>();

    private PendingSlot<ColumnView> result = new PendingSlot<>();
    
    @Override
    public void setFromCache(ColumnView view) {
        result.set(view);
    }

    @Override
    public ColumnView get() {
        return result.get();
    }

    @Override
    public void onNext(FieldValue value) {
        if(value instanceof GeoArea) {
            Extents extents = ((GeoArea) value).getEnvelope();
            coordinates.add(extents.getX1());
            coordinates.add(extents.getY1());
            coordinates.add(extents.getX2());
            coordinates.add(extents.getY2());
        } else {
            coordinates.add(Double.NaN);
            coordinates.add(Double.NaN);
            coordinates.add(Double.NaN);
            coordinates.add(Double.NaN);
        }
    }

    @Override
    public void done() {
        double[] array = new double[coordinates.size()];
        for(int i=0;i<coordinates.size();++i) {
            array[i] = coordinates.get(i);
        }
        result.set(new GeoColumnView(array));
    }
}
