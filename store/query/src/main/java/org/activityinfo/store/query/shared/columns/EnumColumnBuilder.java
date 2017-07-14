package org.activityinfo.store.query.shared.columns;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.query.impl.views.DiscreteStringColumnView8;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.spi.CursorObserver;

import java.util.List;
import java.util.Map;

public class EnumColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;

    private final Map<ResourceId, Integer> labelIndexMap = Maps.newHashMap();
    private final String[] labels;
    private List<Integer> values = Lists.newArrayList();

    public EnumColumnBuilder(PendingSlot<ColumnView> result, EnumType enumType) {
        this.result = result;

        int labelIndex = 0;
        this.labels = new String[enumType.getValues().size()];
        for (EnumItem item : enumType.getValues()) {
            this.labels[labelIndex] = item.getLabel();
            this.labelIndexMap.put(item.getId(), labelIndex);
            labelIndex++;
        }
    }

    private int[] createIndexArray32() {
        int indexes[] = new int[values.size()];
        for (int i = 0; i != indexes.length; ++i) {
            indexes[i] = values.get(i);
        }
        return indexes;
    }

    private byte[] createIndexArray8() {
        byte indexes[] = new byte[values.size()];
        for (int i = 0; i != indexes.length; ++i) {
            indexes[i] = values.get(i).byteValue();
        }
        return indexes;
    }

    @Override
    public void onNext(FieldValue value) {
        values.add(indexOf(value));
    }

    private int indexOf(FieldValue value) {
        if (value instanceof EnumValue) {
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
    public void done() {
        result.set(build());
    }

    @VisibleForTesting
    ColumnView build() {
        if(labels.length < DiscreteStringColumnView8.MAX_COUNT) {
            return build8();
        } else {
            return build32();
        }
    }

    @VisibleForTesting
    ColumnView build8() {
        return new DiscreteStringColumnView8(labels, createIndexArray8());
    }

    @VisibleForTesting
    ColumnView build32() {
        return new DiscreteStringColumnView(labels, createIndexArray32());
    }

}
