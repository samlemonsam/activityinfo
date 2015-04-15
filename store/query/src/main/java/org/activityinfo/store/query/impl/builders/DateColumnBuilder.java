package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.views.DateArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.service.store.CursorObserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateColumnBuilder implements ColumnViewBuilder, CursorObserver<FieldValue> {

    private List<Date> values = new ArrayList<>();

    private final DateReader reader;

    private PendingSlot<ColumnView> result = new PendingSlot<>();

    public DateColumnBuilder(DateReader reader) {
        this.reader = reader;
    }

    @Override
    public void onNext(FieldValue value) {
        values.add(reader.readDate(value));
    }

    @Override
    public void done() {
        result.set(new DateArrayColumnView(values));
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
