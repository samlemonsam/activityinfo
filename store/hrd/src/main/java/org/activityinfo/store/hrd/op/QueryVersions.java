package org.activityinfo.store.hrd.op;


import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.RecordVersion;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class QueryVersions implements Work<List<RecordVersion>> {

    private FormClass formClass;
    private ResourceId recordId;
    private ResourceId parentRecordId;

    public QueryVersions(FormClass formClass) {
        this.formClass = formClass;
    }

    public static QueryVersions of(FormClass formClass, ResourceId recordId) {
        QueryVersions queryVersions = new QueryVersions(formClass);
        queryVersions.recordId = recordId;
        return queryVersions;
    }
    
    public static QueryVersions subRecords(FormClass formClass, ResourceId parentRecordId) {
        QueryVersions queryVersions = new QueryVersions(formClass);
        queryVersions.parentRecordId = parentRecordId;
        return queryVersions;
    }
    
    @Override
    public List<RecordVersion> run() {

        Query<FormRecordSnapshotEntity> query;

        if(recordId != null) {
            Key<FormRecordEntity> recordKey = FormRecordEntity.key(formClass, recordId);
            query = ofy().load()
                    .type(FormRecordSnapshotEntity.class)
                    .ancestor(recordKey);

        } else {
            Key<FormEntity> rootKey = FormEntity.key(formClass);
            query = ofy().load()
                    .type(FormRecordSnapshotEntity.class)
                    .ancestor(rootKey)
                    .filter("parentRecordId", parentRecordId.asString());
        }

        List<RecordVersion> versions = new ArrayList<>();
        
        for (FormRecordSnapshotEntity snapshot : query.iterable()) {
            RecordVersion version = new RecordVersion();
            version.setRecordId(snapshot.getRecordId());
            version.setVersion(snapshot.getVersion());
            version.setUserId(snapshot.getUserId());
            version.setTime(snapshot.getTime().getTime());
            version.setType(snapshot.getType());

            if (formClass.isSubForm()) {
                version.setSubformKind(formClass.getSubFormKind());
                version.setSubformKey(subformKey(snapshot));
            }

            version.getValues().putAll(snapshot.getRecord().toFieldValueMap(formClass));
            versions.add(version);
        }
        return versions;
    }

    private String subformKey(FormRecordSnapshotEntity snapshot) {
        if (formClass.getSubFormKind() == SubFormKind.REPEATING) {
            return "";
        } else { // period
            int indexOf = snapshot.getRecordId().asString().indexOf(snapshot.getParentRecordId());
            return snapshot.getRecordId().asString().substring(indexOf + snapshot.getParentRecordId().length() + 1);
        }
    }
}
