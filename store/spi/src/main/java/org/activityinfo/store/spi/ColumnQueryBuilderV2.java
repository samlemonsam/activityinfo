package org.activityinfo.store.spi;

import org.activityinfo.model.query.ColumnView;

public interface ColumnQueryBuilderV2 {

    void addRecordId(PendingSlot<ColumnView> target);

    void addField(FieldComponent fieldComponent, PendingSlot<ColumnView> target);

    void addRowCount(PendingSlot<Integer> rowCount);

    void execute();
}
