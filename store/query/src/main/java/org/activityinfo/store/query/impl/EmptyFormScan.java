package org.activityinfo.store.query.impl;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.impl.join.ForeignKey;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class EmptyFormScan implements FormScan {
    @Override
    public Slot<ColumnView> addResourceId() {
        return EmptySlots.STRING;
    }

    @Override
    public Slot<Integer> addCount() {
        return EmptySlots.ZERO_ROW_COUNT;
    }

    @Override
    public Slot<ColumnView> addField(ExprNode fieldExpr) {
        return EmptySlots.STRING;
    }

    @Override
    public Slot<ForeignKey> addForeignKey(String fieldName, ResourceId rightFormId) {
        return EmptySlots.EMPTY_FK;
    }

    @Override
    public Slot<ForeignKey> addForeignKey(ExprNode referenceField, ResourceId rightFormId) {
        return EmptySlots.EMPTY_FK;
    }

    @Override
    public Set<String> getCacheKeys() {
        return Collections.emptySet();
    }

    @Override
    public void updateFromCache(Map<String, Object> cached) {

    }

    @Override
    public void execute() throws Exception {

    }

    @Override
    public Map<String, Object> getValuesToCache() {
        return Collections.emptyMap();
    }
}
