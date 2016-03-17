package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.Cursor;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;

/**
 * Column query on a "normal" sql table, where fields are mapped to
 * a fixed column in the table 
 */
public class SimpleTableColumnQueryBuilder implements ColumnQueryBuilder {

    private final MySqlCursorBuilder cursorBuilder;

    public SimpleTableColumnQueryBuilder(MySqlCursorBuilder cursorBuilder) {
        this.cursorBuilder = cursorBuilder;
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        cursorBuilder.addResourceId(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        cursorBuilder.addField(fieldId, observer);
    }

    @Override
    public void execute() {
        Cursor open = cursorBuilder.open();
        while(open.next()) {
        }
    }
}
