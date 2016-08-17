package org.activityinfo.store.mysql.side;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.service.store.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by yuriyz on 5/18/2016.
 */
public class ReferenceBuffer implements ValueBuffer {

    private ReferenceValue value = null;
    private final List<CursorObserver<FieldValue>> observers = Lists.newArrayList();

    @Override
    public void add(CursorObserver<FieldValue> observer) {
        observers.add(observer);
    }

    @Override
    public void set(ResultSet rs) throws SQLException {
        String json = rs.getString(STRING_VALUE_COLUMN);
        if (!Strings.isNullOrEmpty(json)) {
            value = ReferenceValue.fromJson(json);
        } else {
            value = null;
        }
    }

    @Override
    public void next() {
        for (CursorObserver<FieldValue> observer : observers) {
            observer.onNext(value);
        }
        value = null;
    }

    @Override
    public void done() {
        for (CursorObserver<FieldValue> observer : observers) {
            observer.done();
        }
    }
}
