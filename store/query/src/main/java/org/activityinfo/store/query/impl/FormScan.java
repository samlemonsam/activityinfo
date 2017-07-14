package org.activityinfo.store.query.impl;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.impl.join.ForeignKey;

import java.util.Map;
import java.util.Set;

public interface FormScan {

    Slot<ColumnView> addResourceId();

    Slot<Integer> addCount();

    Slot<ColumnView> addField(ExprNode fieldExpr);

    Slot<ForeignKey> addForeignKey(String fieldName, ResourceId rightFormId);

    Slot<ForeignKey> addForeignKey(ExprNode referenceField, ResourceId rightFormId);

    Set<String> getCacheKeys();

    void updateFromCache(Map<String, Object> cached);

    void execute() throws Exception;

    Map<String, Object> getValuesToCache();
}
