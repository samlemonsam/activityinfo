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
package org.activityinfo.store.hrd;

import com.google.common.base.Predicate;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.spi.RecordChangeType;

import java.util.*;

public class SyncSetBuilder {

    private final FormClass formClass;
    private long localVersion;
    private Predicate<ResourceId> visibilityPredicate;

    private Set<String> deleted = new HashSet<>();
    private Map<ResourceId, FormRecordSnapshotEntity> snapshots = new HashMap<>();

    SyncSetBuilder(FormClass formClass, long localVersion, Predicate<ResourceId> visibilityPredicate) {
        this.formClass = formClass;
        this.localVersion = localVersion;
        this.visibilityPredicate = visibilityPredicate;
    }

    public void add(FormRecordSnapshotEntity snapshot) {
        if(visibilityPredicate.apply(snapshot.getRecord().getRecordId())) {
            if (snapshot.getType() == RecordChangeType.DELETED) {
                deleted.add(snapshot.getRecordId().asString());
                snapshots.remove(snapshot.getRecordId());

            } else {
                snapshots.put(snapshot.getRecordId(), snapshot);
            }
        }
    }

    private String[] buildDeletedArray() {
        return deleted.toArray(new String[deleted.size()]);
    }


    private List<FormRecord> buildUpdateArrays() {
        FormRecord[] records = new FormRecord[snapshots.size()];
        int i = 0;
        for (FormRecordSnapshotEntity snapshotEntity : snapshots.values()) {
            records[i++] = snapshotEntity.getRecord().toFormRecord(formClass);
        }
        return Arrays.asList(records);
    }

    public FormSyncSet build() {
        if(localVersion == 0) {
            return FormSyncSet.complete(formClass.getId(), buildUpdateArrays());
        } else {
            return FormSyncSet.incremental(formClass.getId().asString(), buildDeletedArray(), buildUpdateArrays());
        }
    }

}
