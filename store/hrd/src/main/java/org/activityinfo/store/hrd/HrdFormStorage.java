package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Optional;
import com.googlecode.objectify.cmd.Query;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.hrd.op.CreateOrUpdateForm;
import org.activityinfo.store.hrd.op.CreateOrUpdateRecord;
import org.activityinfo.store.hrd.op.QuerySubRecords;
import org.activityinfo.store.hrd.op.QueryVersions;
import org.activityinfo.store.spi.*;

import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.store.hrd.Hrd.ofy;


/**
 * Accessor for forms backed by the AppEngine High-Replication Datastore (HRD)
 */
public class HrdFormStorage implements VersionedFormStorage {

    private long version;
    private FormClass formClass;

    public HrdFormStorage(FormClass formClass) {
        this.formClass = formClass;
    }

    @Override
    public FormPermissions getPermissions(int userId) {
        return FormPermissions.full();
    }

    @Override
    public Optional<FormRecord> get(ResourceId recordId) {

        FormRecordEntity entity = ofy().load().key(FormRecordEntity.key(formClass, recordId)).now();

        if(entity != null) {
            FormRecord record = entity.toFormRecord(formClass);
            return Optional.of(record);
        
        } else {
            return Optional.absent();
        }
    }

    @Override
    public List<RecordVersion> getVersions(ResourceId recordId) {
        return ofy().transact(QueryVersions.of(formClass, recordId));
    }

    @Override
    public List<RecordVersion> getVersionsForParent(ResourceId parentRecordId) {
        return ofy().transact(QueryVersions.subRecords(formClass, parentRecordId));
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        ofy().transact(new CreateOrUpdateForm(formClass));
    }

    @Override
    public void add(TypedRecordUpdate update) {
        ofy().transact(new CreateOrUpdateRecord(formClass.getId(), update));
    }

    @Override
    public void update(final TypedRecordUpdate update) {
        ofy().transact(new CreateOrUpdateRecord(formClass.getId(), update));
    }
    
    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new HrdQueryColumnBuilder(formClass);
    }

    @Override
    public long cacheVersion() {
        FormEntity entity = ofy().load().type(FormEntity.class).id(formClass.getId().asString()).now();
        return entity.getVersion();
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FormRecord> getSubRecords(ResourceId parentId) {
        return ofy().transact(new QuerySubRecords(formClass, parentId));
    }

    @Override
    public FormSyncSet getVersionRange(long localVersion, long toVersion) {

        Query<FormRecordSnapshotEntity> query = ofy().load().type(FormRecordSnapshotEntity.class)
                .ancestor(FormEntity.key(formClass));

        if(localVersion > 0) {
            query = query.filter("version >", localVersion);
        }

        SyncSetBuilder builder = new SyncSetBuilder(getFormClass(), localVersion);

        QueryResultIterator<FormRecordSnapshotEntity> it = query.iterator();
        while(it.hasNext()) {
            FormRecordSnapshotEntity snapshot = it.next();
            builder.add(snapshot);
        }
        return builder.build();
    }
}
