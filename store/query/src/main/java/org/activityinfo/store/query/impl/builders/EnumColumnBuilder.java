package org.activityinfo.store.query.impl.builders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.views.DiscreteStringColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.service.store.CursorObserver;

import java.util.List;
import java.util.Map;

public class EnumColumnBuilder implements ColumnViewBuilder, CursorObserver<FieldValue> {

    private final Map<ResourceId, Integer> labelIndexMap = Maps.newHashMap();
    private final String[] labels;
    private List<Integer> values = Lists.newArrayList();

    private PendingSlot<ColumnView> result = new PendingSlot<>();

    public EnumColumnBuilder(EnumType enumType) {

        int labelIndex = 0;
        this.labels = new String[enumType.getValues().size()];
        for(EnumItem item : enumType.getValues()) {
            this.labels[labelIndex] = item.getLabel();
            this.labelIndexMap.put(item.getId(), labelIndex);
            labelIndex++;
        }
    }

    private int[] createIndexArray() {
        int indexes[] = new int[values.size()];
        for(int i=0;i!=indexes.length;++i) {
            indexes[i] = values.get(i);
        }
        return indexes;
    }

    @Override
    public void onNext(FieldValue value) {
        values.add(indexOf(value));
    }

    private int indexOf(FieldValue value) {
        if(value instanceof EnumValue) {
            EnumValue fieldValue = (EnumValue) value;
            if (fieldValue.getResourceIds().size() == 1) {
                Integer index = labelIndexMap.get(fieldValue.getValueId());
                if (index != null) {
                    return index;
                }
            }
        }
        return -1;
    }

    @Override
    public void onClosed() {
        result.set(new DiscreteStringColumnView(labels, createIndexArray()));
    }

    @Override
    public ColumnView get() {
        return result.get();
    }

    @Override
    public void setFromCache(ColumnView view) {
        result.set(view);
    }
}
