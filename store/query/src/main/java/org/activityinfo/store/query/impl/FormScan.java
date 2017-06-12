package org.activityinfo.store.query.impl;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.join.ForeignKeyMap;

import java.util.Map;
import java.util.Set;

public interface FormScan {

    Slot<ColumnView> addResourceId();

    Slot<Integer> addCount();

    Slot<ColumnView> addField(ExprNode fieldExpr);

    Slot<ForeignKeyMap> addForeignKey(String fieldName);

    Slot<ForeignKeyMap> addForeignKey(ExprNode referenceField);

    Set<String> getCacheKeys();

    void updateFromCache(Map<String, Object> cached);

    void execute() throws Exception;

    Map<String, Object> getValuesToCache();
}
