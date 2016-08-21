package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.RecordVersion;
import org.activityinfo.store.hrd.entity.*;
import org.activityinfo.store.hrd.op.QueryOperation;

import java.util.ArrayList;
import java.util.List;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity.PARENT_PROPERTY;

public class HrdVersionReader {
    
    private Datastore datastore;
    private FormClass formClass;

    public HrdVersionReader(Datastore datastore, FormClass formClass) {
        this.datastore = datastore;
        this.formClass = formClass;
    }

    public List<RecordVersion> read(final ResourceId recordId) {
        FormRecordKey recordKey = new FormRecordKey(formClass.getId(), recordId);
        final Query query = new Query(FormRecordSnapshotKey.KIND, recordKey.raw());

        return query(query);
    }
    
    public List<RecordVersion> readChildren(final ResourceId parentRecordId) {
        FormRootKey formKey = new FormRootKey(formClass.getId());
        final Query query = new Query(FormRecordSnapshotKey.KIND)
                .setAncestor(formKey.raw())
                .setFilter(new Query.FilterPredicate(PARENT_PROPERTY, EQUAL, parentRecordId.asString()));

        return query(query);
    }
    

    private List<RecordVersion> query(final Query query) {
        return datastore.execute(new QueryOperation<List<RecordVersion>>() {
            @Override
            public List<RecordVersion> execute(Datastore datastore) {
                List<RecordVersion> versions = new ArrayList<RecordVersion>();
                for (Entity entity : datastore.prepare(query).asIterable()) {
                    RecordVersion version = new RecordVersion();
                    FormRecordSnapshotEntity snapshot = new FormRecordSnapshotEntity(entity);
                    FormRecordEntity record = snapshot.getFormRecord();

                    version.setRecordId(snapshot.getRecordId());
                    version.setVersion(snapshot.getVersion());
                    version.setUserId(snapshot.getUserId());
                    version.setTime(snapshot.getTime().getTime());
                    version.setType(snapshot.getType());
                    version.getValues().putAll(record.toFieldValueMap(formClass));
                    versions.add(version);
                }
                return versions;
            }
        });
    }

}
