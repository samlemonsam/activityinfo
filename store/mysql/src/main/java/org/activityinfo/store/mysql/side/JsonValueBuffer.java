package org.activityinfo.store.mysql.side;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ParametrizedFieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.service.store.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by yuriyz on 5/18/2016.
 */
public class JsonValueBuffer implements ValueBuffer {

    private FieldValue value = null;
    private final List<CursorObserver<FieldValue>> observers = Lists.newArrayList();

    private final ParametrizedFieldType type;

    public JsonValueBuffer(ParametrizedFieldType type) {
        this.type = type;
    }

    @Override
    public void add(CursorObserver<FieldValue> observer) {
        observers.add(observer);
    }

    @Override
    public void set(ResultSet rs) throws SQLException {
        String json = rs.getString(STRING_VALUE_COLUMN);
        if (!Strings.isNullOrEmpty(json)) {
            if (type instanceof AttachmentType) {
                value = AttachmentValue.fromJson(json);
            } else if (type instanceof ReferenceType) {
                value = ReferenceValue.fromJson(json);
            } else {
                throw new UnsupportedOperationException("Unsupported type: " + type + ", json: " + json);
            }
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
