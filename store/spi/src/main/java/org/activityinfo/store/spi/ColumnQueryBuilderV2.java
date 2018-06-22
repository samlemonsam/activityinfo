package org.activityinfo.store.spi;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

public interface ColumnQueryBuilderV2 extends ColumnQueryBuilder {


    void addField(ResourceId fieldId, PendingSlot<ColumnView> target);

    void addEnumItem(ResourceId fieldId, ResourceId enumId, PendingSlot<ColumnView> target);

    void addFieldComponent(ResourceId fieldId, ResourceId enumId, PendingSlot<ColumnView> target);

    void addRecordId(PendingSlot<ColumnView> target);

    void addRowCount(PendingSlot<Integer> rowCount);
}
