package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;

import java.util.Map;

/**
 * Collection-backed by the AppEngine High-Replication Datastore (HRD)
 */
public class HrdCollection implements ResourceCollection {

    private DatastoreService datastoreService;
    private FormClass formClass;

    public HrdCollection(DatastoreService datastoreService, FormClass formClass) {
        this.datastoreService = datastoreService;
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

        Entity entity = new Entity(CollectionKeys.resourceKey(formClass.getId(), update.getResourceId()));

        if(formClass.getParentFormId().isPresent()) {
            ResourceId parentId = update.getParentId();
            if(parentId == null) {
                throw new IllegalArgumentException("ParentId for subforms is required");
            }
            entity.setProperty("@parent", parentId.asString());
        }
        
        for (Map.Entry<ResourceId, FieldValue> entry : update.getChangedFieldValues().entrySet()) {
            FormField field = formClass.getField(entry.getKey());
            FieldConverter converter = FieldConverters.forType(field.getType());
            if(entry.getValue() != null) {
                entity.setProperty(field.getName(), converter.toHrdProperty(entry.getValue()));
            }
        }
        
        Transaction tx = datastoreService.beginTransaction(TransactionOptions.Builder.withDefaults());
        try {
            datastoreService.put(tx, entity);
            tx.commit();
        } finally {
            if(tx.isActive()) {
                tx.rollback();
            }
        }
    }

    @Override
    public void update(ResourceUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new HrdQueryColumnBuilder(datastoreService, CollectionKeys.collectionKey(formClass.getId()), formClass);
    }

    @Override
    public long cacheVersion() {
        return 0;
    }
}
