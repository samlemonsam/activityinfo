package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.query.shared.join.PrimaryKeyMap;
import org.activityinfo.store.spi.CursorObserver;

/**
 * Creates optimizing column builder.
 *
 * <p>We use different implementations on the server and on the client
 * that use different types of optimizations.</p>
 */
public interface ColumnFactory {

    CursorObserver<FieldValue> newStringBuilder(PendingSlot<ColumnView> result, StringReader reader);

    CursorObserver<FieldValue> newDoubleBuilder(PendingSlot<ColumnView> result, DoubleReader reader);

    CursorObserver<FieldValue> newEnumBuilder(PendingSlot<ColumnView> result, EnumType enumType);

    CursorObserver<FieldValue> newForeignKeyBuilder(ResourceId rightFormId, PendingSlot<ForeignKey> value);

    PrimaryKeyMap newPrimaryKeyMap(ColumnView columnView);
}
