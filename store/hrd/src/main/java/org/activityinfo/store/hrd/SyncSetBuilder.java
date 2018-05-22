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

import com.google.apphosting.api.ApiProxy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.spi.RecordChangeType;

import java.util.*;
import java.util.function.Predicate;

public class SyncSetBuilder {

    private static final long BUFFER_MS = 5_000;
    private static final int MAX_RESPONSE_SIZE = 200_000;

    private final FormClass formClass;
    private long localVersion;
    private Predicate<ResourceId> visibilityPredicate;
    private Optional<String> cursor = Optional.empty();

    private Set<String> deleted = new HashSet<>();
    private Map<ResourceId, FormRecordSnapshotEntity> snapshots = new HashMap<>();

    private long estimatedSizeInBytes = 0;

    SyncSetBuilder(FormClass formClass, long localVersion, Predicate<ResourceId> visibilityPredicate) {
        this.formClass = formClass;
        this.localVersion = localVersion;
        this.visibilityPredicate = visibilityPredicate;
    }

    /**
     * Adds this snapshot to the {@link FormSyncSet}
     * @param snapshot
     * @return {@code true} if there is time and space left to continue adding more snapshots,
     * or if we should stop here.
     */
    public boolean add(FormRecordSnapshotEntity snapshot) {
        if(visibilityPredicate.test(snapshot.getRecord().getRecordId())) {
            if (snapshot.getType() == RecordChangeType.DELETED) {
                String recordId = snapshot.getRecordId().asString();
                if(deleted.add(recordId)) {
                    estimatedSizeInBytes += recordId.length();
                }

                if(snapshots.remove(snapshot.getRecordId()) != null) {
                    estimatedSizeInBytes -= estimateSizeInBytes(snapshot);
                }
            } else {
                if(snapshots.put(snapshot.getRecordId(), snapshot) == null) {
                    estimatedSizeInBytes += estimateSizeInBytes(snapshot);
                }
            }
        }

        // Do we have enough time left in this request to do any more work?
        long timeRemaining = ApiProxy.getCurrentEnvironment().getRemainingMillis();
        if(timeRemaining < BUFFER_MS) {
            return false;
        }

        if(estimatedSizeInBytes > MAX_RESPONSE_SIZE) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return a very rough estimate of how many bytes of JSON this response will require so far.
     */
    public long getEstimatedSizeInBytes() {
        return estimatedSizeInBytes;
    }

    private long estimateSizeInBytes(FormRecordSnapshotEntity snapshot) {
        int numFields = snapshot.getRecord().getFieldValues().getProperties().size();
        return 200 + (numFields * 20);
    }

    public void stop(String cursor) {
        this.cursor = Optional.of(cursor);
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
            return FormSyncSet.initial(formClass.getId(), buildUpdateArrays(), cursor);
        } else {
            return FormSyncSet.incremental(formClass.getId().asString(), buildDeletedArray(), buildUpdateArrays(), cursor);
        }
    }

}
