package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.api.client.FormHistoryEntryBuilder;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormPermissions;
import org.activityinfo.store.hrd.entity.Datastore;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordKey;
import org.activityinfo.store.hrd.entity.FormRootKey;
import org.activityinfo.store.hrd.op.CreateOrUpdateForm;
import org.activityinfo.store.hrd.op.CreateOrUpdateRecord;
import org.activityinfo.store.hrd.op.QueryOperation;

import java.util.List;

/**
 * Accessor for forms backed by the AppEngine High-Replication Datastore (HRD)
 */
public class HrdFormAccessor implements FormAccessor {

    private Datastore datastore;
    private FormClass formClass;

    public HrdFormAccessor(Datastore datastore, FormClass formClass) {
        this.datastore = datastore;
        this.formClass = formClass;
    }

    @Override
    public FormPermissions getPermissions(int userId) {
        return FormPermissions.full();
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        FormRecordKey key = new FormRecordKey(resourceId);
        Optional<FormRecordEntity> submission = datastore.loadIfPresent(key);

        if(submission.isPresent()) {
            FormRecord record = submission.get().toFormRecord(formClass);
            return Optional.of(record);
        
        } else {
            return Optional.absent();
        }
    }

    @Override
    public List<FormHistoryEntryBuilder> getHistory(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        datastore.execute(new CreateOrUpdateForm(formClass));
    }

    @Override
    public void add(RecordUpdate update) {
        datastore.execute(new CreateOrUpdateRecord(formClass.getId(), update));
    }

    @Override
    public void update(final RecordUpdate update) {
        datastore.execute(new CreateOrUpdateRecord(formClass.getId(), update));
    }
    

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new HrdQueryColumnBuilder(datastore.unwrap(), FormRootKey.key(formClass.getId()), formClass);
    }

    @Override
    public long cacheVersion() {
        return 0;
    }

    public FormRecord getSubmission(ResourceId resourceId) throws EntityNotFoundException {
        FormRecordKey key = new FormRecordKey(resourceId);
        FormRecordEntity submission = datastore.load(key);
        
        return submission.toFormRecord(formClass);
    }

    public Iterable<FormRecord> getSubmissionsOfParent(ResourceId parentId) {
        return getSubmissions(parentId);
    }

    public Iterable<FormRecord> getSubmissions(ResourceId parentId) {
        List<Query.Filter> filters = Lists.newArrayList();
        filters.add(FormRecordEntity.deletedFilter(false));

        if (parentId != null) {
            filters.add(FormRecordEntity.parentFilter(parentId));
        }
        if (filters.size() == 1) {
            return query(filters.get(0));
        }
        return query(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));
    }

    private Iterable<FormRecord> query(Query.Filter filter) {
        FormRootKey rootKey = new FormRootKey(formClass.getId());
        final Query query = new Query(FormRecordEntity.KIND, rootKey.raw());
        query.setFilter(filter);

        return datastore.execute(new QueryOperation<List<FormRecord>>() {
            @Override
            public List<FormRecord> execute(Datastore datastore) {
                List<FormRecord> instances = Lists.newArrayList();
                for (Entity entity : datastore.prepare(query).asIterable()) {
                    FormRecordEntity submission = new FormRecordEntity(entity);
                    instances.add(submission.toFormRecord(formClass));
                }
                return instances;
            }
        });
    }
}
