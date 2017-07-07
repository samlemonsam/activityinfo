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
        return FormSyncSet.create(formClass.getId().asString(), buildDeletedArray(), buildUpdateArrays());
    }

}
