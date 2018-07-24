/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.store.query.server.columns;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.query.shared.columns.MultiDiscreteStringColumnView;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.PendingSlot;

import java.util.BitSet;
import java.util.Map;

/**
 * Constructs ColumnViews for fields with multi-select enumerated type
 *
 */
public class MultiEnumColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;

    private final Map<ResourceId, Integer> labelIndexMap = Maps.newHashMap();
    private final String[] ids;
    private final String[] labels;
    private BitSet[] selections;
    private int n = 0;

    public MultiEnumColumnBuilder(PendingSlot<ColumnView> result, EnumType enumType) {
        this.result = result;

        int index = 0;
        this.ids = new String[enumType.getValues().size()];
        this.labels = new String[enumType.getValues().size()];
        this.selections = new BitSet[enumType.getValues().size()];

        for (EnumItem item : enumType.getValues()) {
            this.ids[index] = item.getId().asString();
            this.labels[index] = item.getLabel();
            this.labelIndexMap.put(item.getId(), index);
            this.selections[index] = new BitSet();
            index++;
        }
    }

    @Override
    public void onNext(FieldValue value) {
        if (!(value instanceof EnumValue)) {
            clearAll(n);
            n++;
            return;
        }

        EnumValue fieldValue = (EnumValue) value;
        labelIndexMap.forEach((id, index) -> {
            if (fieldValue.getResourceIds().contains(id)) {
                selections[index].set(n);
            } else {
                selections[index].clear(n);
            }
        });

        n++;
    }

    private void clearAll(int rowIndex) {
        for (int i=0; i < selections.length; i++) {
            selections[i].clear(rowIndex);
        }
    }

    @Override
    public void done() {
        result.set(build());
    }

    @VisibleForTesting
    ColumnView build() {
        return new MultiDiscreteStringColumnView(n, ids, labels, selections);
    }

}
