package org.activityinfo.store.query.impl.builders;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.views.ConstantColumnView;
import org.activityinfo.store.query.impl.views.EmptyColumnView;
import org.activityinfo.store.query.impl.views.StringArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.service.store.CursorObserver;

import java.util.List;

public class StringColumnBuilder implements ColumnViewBuilder, CursorObserver<FieldValue> {

    private List<String> values = Lists.newArrayList();

    // Keep track of some statistics to
    private StringStatistics stats = new StringStatistics();

    private PendingSlot<ColumnView> result = new PendingSlot<>();

    private StringReader reader;

    public StringColumnBuilder(StringReader reader) {
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
            result.set(new EmptyColumnView(values.size(), ColumnType.STRING));

        } else if(stats.isConstant()) {
            result.set(new ConstantColumnView(values.size(), values.get(0)));

        } else {
            result.set(new StringArrayColumnView(values));
        }
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
