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
package org.activityinfo.store.query.client.join;

import com.google.common.annotations.VisibleForTesting;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.query.shared.columns.ForeignKey;
import org.activityinfo.store.query.shared.columns.ForeignKey32;
import org.activityinfo.store.spi.ForeignKeyBuilder;
import org.activityinfo.store.spi.PendingSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Super simple foreign key builder that can be translated to JavaScript
 */
public class SimpleForeignKeyBuilder implements ForeignKeyBuilder {


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
    private Map<String, Integer> keyMap = new HashMap<>();

    /**
     * Vector that contains an entry for each row, with the key index as value.
     */
    private List<Integer> keys = new ArrayList<>();


    public SimpleForeignKeyBuilder(ResourceId rightFormId, PendingSlot<ForeignKey> result) {
        this.result = result;
        this.rightFormId = rightFormId;
    }

    @Override
    public void onNext(FieldValue fieldValue) {
        int keyId = -1;

        if(fieldValue instanceof ReferenceValue) {
            String key = ((ReferenceValue) fieldValue).getOnlyRecordId(rightFormId);
            if(key != null) {
                keyId = keyId(key);
            }
        }
        keys.add(keyId);
    }


    @Override
    public void onNextId(String id) {
        if(id == null) {
            keys.add(-1);
        } else {
            keys.add(keyId(id));
        }
    }


    /**
     * Finds or creates an integer key id for this String key.
     */
    private int keyId(String stringKey) {
        Integer keyId = keyMap.get(stringKey);
        if(keyId == null) {
            keyId = keyMap.size();
            keyMap.put(stringKey, keyId);
        }
        return keyId;
    }


    @Override
    public void done() {
        result.set(build());
    }

    @VisibleForTesting
    ForeignKey build() {
        return new ForeignKey32(keyList(), buildIntArray(), keys.size());
    }

    private String[] keyList() {
        String[] keys = new String[keyMap.size()];
        for (Map.Entry<String, Integer> entry : keyMap.entrySet()) {
            keys[entry.getValue()] = entry.getKey();
        }
        return keys;
    }

    private int[] buildIntArray() {
        int array[] = new int[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            array[i] = keys.get(i);
        }
        return array;
    }

}
