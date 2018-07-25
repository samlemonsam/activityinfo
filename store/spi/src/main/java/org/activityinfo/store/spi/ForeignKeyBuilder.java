package org.activityinfo.store.spi;

import org.activityinfo.model.type.FieldValue;

public interface ForeignKeyBuilder extends CursorObserver<FieldValue> {

    void onNextId(String id);
}
