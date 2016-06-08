package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.*;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.ResourceCollection;

import java.util.*;

/**
 * Catalog of Collection hosted in the AppEngine High Replication Datastore
 */
public class HrdCatalog implements CollectionCatalog {
    
    private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
    

    public void create(FormClass formClass) {
        Entity entity = RecordSerialization.toFormClassEntity(formClass);
        datastoreService.put(entity);
    }
    
    @Override
    public Optional<ResourceCollection> getCollection(ResourceId collectionId) {

        Entity entity;
        try {
            entity = datastoreService.get(CollectionKeys.formClassKey(collectionId));
        } catch (EntityNotFoundException e) {
            return Optional.absent();
        }
        
        FormClass formClass = RecordSerialization.fromFormClassEntity(entity);
        return Optional.<ResourceCollection>of(new HrdCollection(datastoreService, formClass));
    }

    @Override
    public Optional<ResourceCollection> lookupCollection(ResourceId resourceId) {
        if(resourceId.getDomain() != ResourceId.GENERATED_ID_DOMAIN) {
            return Optional.absent();
        }
        String parts[] = resourceId.asString().split("-");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Expected resource id in the form c00000-000000");
        }
        return getCollection(ResourceId.valueOf(parts[0]));
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> collectionIds) {
        
        Set<Key> toLoad = new HashSet<>();
        for (ResourceId collectionId : collectionIds) {
            toLoad.add(CollectionKeys.formClassKey(collectionId));
        }
        Map<Key, Entity> entityMap = datastoreService.get(toLoad);
        Map<ResourceId, FormClass> formClassMap = new HashMap<>();
        
        for (ResourceId collectionId : collectionIds) {
            Key key = CollectionKeys.formClassKey(collectionId);
            Entity entity = entityMap.get(key);
            if(entity != null) {
                FormClass formClass = RecordSerialization.fromFormClassEntity(entity);
                formClassMap.put(collectionId, formClass);
            }
        }

        return formClassMap;
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getCollection(resourceId).get().getFormClass();
    }
}
