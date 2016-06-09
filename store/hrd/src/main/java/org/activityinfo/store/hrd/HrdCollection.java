package org.activityinfo.store.hrd;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.hrd.entity.CollectionRootKey;
import org.activityinfo.store.hrd.entity.Datastore;
import org.activityinfo.store.hrd.op.CreateOrUpdateSubmission;

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
        throw new UnsupportedOperationException();
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
}
