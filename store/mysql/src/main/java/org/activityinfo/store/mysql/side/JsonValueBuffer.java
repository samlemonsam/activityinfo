package org.activityinfo.store.mysql.side;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonParser;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by yuriyz on 5/18/2016.
 */
public class JsonValueBuffer implements ValueBuffer {

    private FieldValue value = null;
    private final List<CursorObserver<FieldValue>> observers = Lists.newArrayList();

    private final FieldType type;
    private final JsonParser parser = new JsonParser();

    public JsonValueBuffer(FieldType type) {
        this.type = type;
    }

    @Override
    public void add(CursorObserver<FieldValue> observer) {
        observers.add(observer);
    }

    @Override
    public void set(ResultSet rs) throws SQLException {
        String json = rs.getString(STRING_VALUE_COLUMN);
        if (Strings.isNullOrEmpty(json)) {
            value = null;
        } else {
            value = type.parseJsonValue(parser.parse(json));
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
