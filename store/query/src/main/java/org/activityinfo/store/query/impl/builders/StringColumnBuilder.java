package org.activityinfo.store.query.impl.builders;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.*;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.query.impl.PendingSlot;

import java.util.List;

public class StringColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;


    private List<String> values = Lists.newArrayList();

    // Keep track of some statistics to
    private StringStatistics stats = new StringStatistics();


    private StringReader reader;

    public StringColumnBuilder(PendingSlot<ColumnView> result, StringReader reader) {
        this.result = result;
        this.reader = reader;
    }

    @Override
    public void onNext(FieldValue value) {
        String string = null;
        if(value != null) {
            string = reader.readString(value);
        }
        stats.update(string);
        values.add(string);
    }

    @Override
    public void done() {
        if(stats.isEmpty()) {
            result.set(new EmptyColumnView(ColumnType.STRING, values.size()));

        } else if(stats.isConstant()) {
            result.set(new ConstantColumnView(values.size(), values.get(0)));

        } else {
            result.set(new StringArrayColumnView(values));
        }
    }
}
