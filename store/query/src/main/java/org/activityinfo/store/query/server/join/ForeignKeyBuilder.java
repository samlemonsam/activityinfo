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
package org.activityinfo.store.query.server.join;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.query.shared.columns.ForeignKey;
import org.activityinfo.store.query.shared.columns.ForeignKey32;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.PendingSlot;

/**
 * Constructs a ForeignKey by listening for a cursor.
 *
 */
public class ForeignKeyBuilder implements CursorObserver<FieldValue> {


    /**
     * The form against which we are joining.
     *
     * <p>An ActivityInfo reference field can reference more than one form, but for this
     * data structure, we are only interesting in mapping each row to a unique row in a specific
     * referenced form.</p>
     */
    private final ResourceId rightFormId;

    private final PendingSlot<ForeignKey> result;

    /**
     * Map from key to key index.
     *
     * <p>Foreign keys are stored as strings and can be quite long. There does, however, tend to be alot
     * of repetition. To avoid using far more storage that we need, particularly when serializing to memcache,
     * we maintain a list of unique foreign keys and assign them a new integer id.</p>
     */
    private Object2IntOpenHashMap<String> keyMap = new Object2IntOpenHashMap<>();

    /**
     * Vector that contains an entry for each row, with the key index as value.
     */
    private IntArrayList keys = new IntArrayList();



    public ForeignKeyBuilder(ResourceId rightFormId, PendingSlot<ForeignKey> result) {
        this.result = result;
        this.rightFormId = rightFormId;
        keyMap.defaultReturnValue(-1);
    }

    @Override
    public void onNext(FieldValue fieldValue) {
        int count = 0;
        int key = -1;

        if(fieldValue instanceof ReferenceValue) {
            ReferenceValue referenceValue = (ReferenceValue) fieldValue;
            for (RecordRef id : referenceValue.getReferences()) {
                if(id.getFormId().equals(rightFormId)) {
                    count++;
                    key = keyId(id);
                }
            }
        }
        if(count == 1) {
            keys.add(key);
        } else {
            keys.add(-1);
        }
    }

    /**
     * Finds or creates an integer key id for this String key.
     */
    private int keyId(RecordRef id) {
        String stringKey = id.getRecordId().asString();
        int keyIndex = keyMap.getInt(stringKey);
        if(keyIndex == -1) {
            keyIndex = keyMap.size();
            keyMap.put(stringKey, keyIndex);
        }
        return keyIndex;
    }

    private String[] keyList() {
        String[] keys = new String[keyMap.size()];
        for (Object2IntMap.Entry<String> entry : keyMap.object2IntEntrySet()) {
            keys[entry.getIntValue()] = entry.getKey();
        }
        return keys;
    }

    @Override
    public void done() {
        result.set(build());
    }

    @VisibleForTesting
    ForeignKey build() {
        return new ForeignKey32(keyList(), keys.elements(), keys.size());
    }

}
