package org.activityinfo.store.mysql.side;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
* Created by alex on 1/20/15.
*/
class TextBuffer implements ValueBuffer {
    private TextValue value = null;
    private CursorObserver<FieldValue> observer;

    public TextBuffer(CursorObserver<FieldValue> observer) {
        this.observer = observer;
    }

    @Override
    public void set(ResultSet rs) throws SQLException {
        value = TextValue.valueOf(rs.getString(STRING_VALUE_COLUMN));
    }

    @Override
    public void next() {
        observer.onNext(value);
        value = null;
    }

    @Override
    public void done() {
        observer.done();
    }
}
