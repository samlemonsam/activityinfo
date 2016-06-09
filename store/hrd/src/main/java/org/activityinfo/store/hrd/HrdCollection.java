package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.hrd.entity.CollectionRootKey;
import org.activityinfo.store.hrd.entity.Datastore;
import org.activityinfo.store.hrd.entity.FormSubmission;
import org.activityinfo.store.hrd.entity.FormSubmissionKey;
import org.activityinfo.store.hrd.op.CreateOrUpdateCollection;
import org.activityinfo.store.hrd.op.CreateOrUpdateSubmission;
import org.activityinfo.store.hrd.op.QueryOperation;

import java.util.List;

/**
 * Collection-backed by the AppEngine High-Replication Datastore (HRD)
 */
public class HrdCollection implements ResourceCollection {

    private Datastore datastore;
    private FormClass formClass;

    public HrdCollection(Datastore datastore, FormClass formClass) {
        this.datastore = datastore;
        this.formClass = formClass;
    }

    @Override
    public CollectionPermissions getPermissions(int userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Resource> get(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        datastore.execute(new CreateOrUpdateCollection(formClass));
    }

    @Override
    public void add(ResourceUpdate update) {
        datastore.execute(new CreateOrUpdateSubmission(formClass.getId(), update));
    }

    @Override
    public void update(final ResourceUpdate update) {
        datastore.execute(new CreateOrUpdateSubmission(formClass.getId(), update));
    }
    

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new HrdQueryColumnBuilder(datastore.unwrap(), CollectionRootKey.key(formClass.getId()), formClass);
    }

    @Override
    public long cacheVersion() {
        return 0;
    }

    public FormInstance getSubmission(ResourceId resourceId) throws EntityNotFoundException {
        FormSubmissionKey key = new FormSubmissionKey(resourceId);
        FormSubmission submission = datastore.load(key);
        
        return submission.toFormInstance(formClass);
    }

    public Iterable<FormInstance> getSubmissionsOfParent(ResourceId parentId) {
        return query(FormSubmission.parentFilter(parentId));
    }

    public Iterable<FormInstance> getSubmissions() {
        return query(null);
    }

    private Iterable<FormInstance> query(Query.FilterPredicate filter) {
        CollectionRootKey rootKey = new CollectionRootKey(formClass.getId());
        final Query query = new Query(FormSubmission.KIND, rootKey.raw());
        query.setFilter(filter);

        return datastore.execute(new QueryOperation<List<FormInstance>>() {
            @Override
            public List<FormInstance> execute(Datastore datastore) {
                List<FormInstance> instances = Lists.newArrayList();
                for (Entity entity : datastore.prepare(query).asIterable()) {
                    FormSubmission submission = new FormSubmission(entity);
                    instances.add(submission.toFormInstance(formClass));
                }
                return instances;
            }
        });
    }
}
