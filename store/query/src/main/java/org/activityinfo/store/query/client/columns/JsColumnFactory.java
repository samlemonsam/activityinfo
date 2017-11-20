package org.activityinfo.store.query.client.columns;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.store.query.client.join.SimpleForeignKeyBuilder;
import org.activityinfo.store.query.client.join.SimplePrimaryKeyMap;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.query.shared.columns.*;
import org.activityinfo.store.query.shared.join.PrimaryKeyMap;
import org.activityinfo.store.spi.CursorObserver;

/**
 * Column Factory optimized for translation to JavaScript
 */
public class JsColumnFactory implements ColumnFactory {

    public static final JsColumnFactory INSTANCE = new JsColumnFactory();

    @Override
    public CursorObserver<FieldValue> newStringBuilder(PendingSlot<ColumnView> result, StringReader reader) {
        return new StringColumnBuilder(result, reader);
    }

    @Override
    public CursorObserver<FieldValue> newDoubleBuilder(PendingSlot<ColumnView> result, DoubleReader reader) {
        return new SimpleDoubleColumnBuilder(result, reader);
    }

    @Override
    public CursorObserver<FieldValue> newEnumBuilder(PendingSlot<ColumnView> result, EnumType enumType) {
        return new SimpleEnumColumnBuilder(result, enumType);
    }

    @Override
    public CursorObserver<FieldValue> newForeignKeyBuilder(ResourceId rightFormId, PendingSlot<ForeignKey> value) {
        return new SimpleForeignKeyBuilder(rightFormId, value);
    }

    @Override
    public PrimaryKeyMap newPrimaryKeyMap(ColumnView columnView) {
        return new SimplePrimaryKeyMap(columnView);
    }
}
