package org.activityinfo.store.query.server;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.store.query.server.columns.CompactingDoubleColumnBuilder;
import org.activityinfo.store.query.server.columns.CompactingEnumColumnBuilder;
import org.activityinfo.store.query.server.join.FastPrimaryKeyMap;
import org.activityinfo.store.query.server.join.ForeignKeyBuilder;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.query.shared.columns.*;
import org.activityinfo.store.query.shared.join.PrimaryKeyMap;
import org.activityinfo.store.spi.CursorObserver;


public class ServerColumnFactory implements ColumnFactory {

    public static final ServerColumnFactory INSTANCE = new ServerColumnFactory();

    private ServerColumnFactory() {
    }

    @Override
    public CursorObserver<FieldValue> newStringBuilder(PendingSlot<ColumnView> result, StringReader reader) {
        return new StringColumnBuilder(result, reader);
    }

    @Override
    public CursorObserver<FieldValue> newDoubleBuilder(PendingSlot<ColumnView> result, DoubleReader reader) {
        return new CompactingDoubleColumnBuilder(result, reader);
    }

    @Override
    public CursorObserver<FieldValue> newEnumBuilder(PendingSlot<ColumnView> result, EnumType enumType) {
        return new CompactingEnumColumnBuilder(result, enumType);
    }

    @Override
    public CursorObserver<FieldValue> newForeignKeyBuilder(ResourceId rightFormId, PendingSlot<ForeignKey> value) {
        return new ForeignKeyBuilder(rightFormId, value);
    }

    @Override
    public PrimaryKeyMap newPrimaryKeyMap(ColumnView columnView) {
        return new FastPrimaryKeyMap(columnView);
    }
}
