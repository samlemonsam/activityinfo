package org.activityinfo.store.query.impl.builders;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.views.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.service.store.CursorObserver;

import java.util.List;

public class DoubleColumnBuilder implements ColumnViewBuilder, CursorObserver<FieldValue> {

    private final List<Double> values = Lists.newArrayList();

    private DoubleReader reader;

    private PendingSlot<ColumnView> result = new PendingSlot<>();

    public DoubleColumnBuilder(DoubleReader reader) {
        this.reader = reader;
    }

    @Override
    public void onNext(FieldValue value) {
        values.add(reader.read(value));
    }

    @Override
    public void done() {

        double array[] = new double[values.size()];
        for(int i=0;i!=array.length;++i) {
            array[i] = values.get(i);
        }
        result.set(new DoubleArrayColumnView(array));
    }

    @Override
    public ColumnView get() {
        return result.get();
    }

    @Override
    public void setFromCache(ColumnView view) {
        result.set(view);
    }
}
