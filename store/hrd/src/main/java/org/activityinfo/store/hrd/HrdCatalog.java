package org.activityinfo.store.hrd;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.hrd.entity.Datastore;
import org.activityinfo.store.hrd.entity.FormClassEntity;
import org.activityinfo.store.hrd.entity.FormClassKey;
import org.activityinfo.store.hrd.op.CreateOrUpdateCollection;

import java.util.*;

/**
 * Catalog of Collection hosted in the AppEngine High Replication Datastore
 */
public class HrdCatalog implements CollectionCatalog {
    
    private Datastore datastore = new Datastore();

    public HrdCollection create(FormClass formClass) {
        datastore.execute(new CreateOrUpdateCollection(formClass));
        
        return new HrdCollection(datastore, formClass);
    }
    
    @Override
    public Optional<ResourceCollection> getCollection(ResourceId collectionId) {

        Optional<FormClassEntity> formClassEntity = datastore.loadIfPresent(new FormClassKey(collectionId));
        
        if(!formClassEntity.isPresent()) {
            return Optional.absent();
        }

        HrdCollection collection = new HrdCollection(datastore, formClassEntity.get().readFormClass());
        
        return Optional.<ResourceCollection>of(collection);
    }

    @Override
    public Optional<ResourceCollection> lookupCollection(ResourceId resourceId) {
        if(resourceId.getDomain() != ResourceId.GENERATED_ID_DOMAIN) {
            return Optional.absent();
        }
        String parts[] = resourceId.asString().split("-");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Invalid submission id: " + resourceId + 
                    ". Expected format c00000-000000");
        }
        return getCollection(ResourceId.valueOf(parts[0]));
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> collectionIds) {
        
        Set<FormClassKey> toLoad = new HashSet<>();
        for (ResourceId collectionId : collectionIds) {
            toLoad.add(new FormClassKey(collectionId));
        }
        Map<FormClassKey, FormClassEntity> entityMap = datastore.get(toLoad);
        Map<ResourceId, FormClass> formClassMap = new HashMap<>();

        for (Map.Entry<FormClassKey, FormClassEntity> entry : entityMap.entrySet()) {
            formClassMap.put(entry.getKey().getCollectionId(), entry.getValue().readFormClass());
        }
        
        return formClassMap;
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getCollection(resourceId).get().getFormClass();
    }
}
