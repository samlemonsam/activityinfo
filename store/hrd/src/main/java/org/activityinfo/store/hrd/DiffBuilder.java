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

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Stopwatch;
import com.googlecode.objectify.cmd.Query;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.spi.RecordChangeType;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static org.activityinfo.store.hrd.Hrd.ofy;

public class DiffBuilder {

    private static final Logger LOGGER = Logger.getLogger(DiffBuilder.class.getName());

    private final FormClass formClass;
    private Predicate<ResourceId> visibilityPredicate;
    private Optional<String> cursor = Optional.empty();

    private Set<String> deleted = new HashSet<>();
    private Map<ResourceId, FormRecordSnapshotEntity> snapshots = new HashMap<>();

    private final SyncSizeEstimator sizeEstimator = new SyncSizeEstimator();

    DiffBuilder(FormClass formClass, Predicate<ResourceId> visibilityPredicate) {
        this.formClass = formClass;
        this.visibilityPredicate = visibilityPredicate;
    }

    public void query(long localVersion, long toVersion, Optional<String> startAt) {
        LOGGER.info("Starting VersionRange query...");
        Stopwatch stopwatch = Stopwatch.createStarted();

        Query<FormRecordSnapshotEntity> query = ofy().load().type(FormRecordSnapshotEntity.class)
                .ancestor(FormEntity.key(formClass))
                .filter("version >", localVersion)
                .chunk(500);

        if(startAt.isPresent()) {
            query = query.startAt(Cursor.fromWebSafeString(startAt.get()));
        }

        QueryResultIterator<FormRecordSnapshotEntity> it = query.iterator();
        while(it.hasNext()) {
            FormRecordSnapshotEntity snapshot = it.next();
            if(snapshot.getVersion() <= toVersion) {
                add(snapshot);
                if(!sizeEstimator.timeAndSpaceRemaining()) {
                    stop(it.getCursor().toWebSafeString());
                    break;
                }
            }
        }

        LOGGER.info("VersionRange query complete in " + stopwatch.elapsed(TimeUnit.SECONDS) +
                " with estimate size: " + sizeEstimator.getEstimatedSizeInBytes() + " bytes");
    }

    /**
     * Adds this snapshot to the {@link FormSyncSet}
     */
    public void add(FormRecordSnapshotEntity snapshot) {
        if(visibilityPredicate.test(snapshot.getRecord().getRecordId())) {
            if (snapshot.getType() == RecordChangeType.DELETED) {
                String recordId = snapshot.getRecordId().asString();
                if(deleted.add(recordId)) {
                    sizeEstimator.deleteRecord(recordId);
                }

                if(snapshots.remove(snapshot.getRecordId()) != null) {
                    sizeEstimator.minus(snapshot);
                }
            } else {
                if(snapshots.put(snapshot.getRecordId(), snapshot) == null) {
                    sizeEstimator.plus(snapshot);
                }
            }
        }
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
        return FormSyncSet.incremental(formClass.getId().asString(), buildDeletedArray(), buildUpdateArrays(), cursor);
    }

}
