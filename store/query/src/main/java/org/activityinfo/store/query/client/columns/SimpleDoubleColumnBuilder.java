package org.activityinfo.store.query.client.columns;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.query.shared.columns.DoubleReader;
import org.activityinfo.store.spi.CursorObserver;

import java.util.List;

/**
 * Naive implementation of a DoubleColumnBuilder for compilation to JavaScript.
 *
 * <p>This might be improved by using JS typed Arrays, but we need good benchmarks for
 * we can start optimizing.</p>
 */
class SimpleDoubleColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;

    private final List<Double> values = Lists.newArrayList();

    private DoubleReader reader;

    SimpleDoubleColumnBuilder(PendingSlot<ColumnView> result, DoubleReader reader) {
        this.result = result;
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
}
