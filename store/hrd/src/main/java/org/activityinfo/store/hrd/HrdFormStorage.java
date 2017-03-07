package org.activityinfo.store.hrd;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.op.CreateOrUpdateForm;
import org.activityinfo.store.hrd.op.CreateOrUpdateRecord;
import org.activityinfo.store.hrd.op.QuerySubRecords;
import org.activityinfo.store.hrd.op.QueryVersions;
import org.activityinfo.store.spi.*;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Accessor for forms backed by the AppEngine High-Replication Datastore (HRD)
 */
public class HrdFormStorage implements FormStorage {

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
    public void add(RecordUpdate update) {
        ofy().transact(new CreateOrUpdateRecord(formClass.getId(), update));
    }

    @Override
    public void update(final RecordUpdate update) {
        ofy().transact(new CreateOrUpdateRecord(formClass.getId(), update));
    }
    
    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new HrdQueryColumnBuilder(formClass);
    }

    @Override
    public long cacheVersion() {
        return 0;
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        throw new UnsupportedOperationException();
    }

    public Iterable<FormRecord> getSubRecords(ResourceId parentId) {
        return ofy().transact(new QuerySubRecords(formClass, parentId));
    }
}
