package org.activityinfo.store.hrd;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormCatalog;
import org.activityinfo.store.hrd.entity.Datastore;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.hrd.entity.FormSchemaKey;
import org.activityinfo.store.hrd.op.CreateOrUpdateForm;

import java.util.*;

/**
 * Catalog of Collection hosted in the AppEngine High Replication Datastore
 */
public class HrdCatalog implements FormCatalog {
    
    private Datastore datastore = new Datastore();

    public HrdFormAccessor create(FormClass formClass) {
        datastore.execute(new CreateOrUpdateForm(formClass));
        
        return new HrdFormAccessor(datastore, formClass);
    }
    
    @Override
    public Optional<FormAccessor> getForm(ResourceId formId) {

        Optional<FormSchemaEntity> formClassEntity = datastore.loadIfPresent(new FormSchemaKey(formId));
        
        if(!formClassEntity.isPresent()) {
            return Optional.absent();
        }

        HrdFormAccessor collection = new HrdFormAccessor(datastore, formClassEntity.get().readFormClass());
        
        return Optional.<FormAccessor>of(collection);
    }

    @Override
    public Optional<FormAccessor> lookupForm(ResourceId recordId) {
        if(recordId.getDomain() != ResourceId.GENERATED_ID_DOMAIN) {
            return Optional.absent();
        }
        String parts[] = recordId.asString().split("-");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Invalid submission id: " + recordId + 
                    ". Expected format c00000-000000");
        }
        return getForm(ResourceId.valueOf(parts[0]));
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        
        Set<FormSchemaKey> toLoad = new HashSet<>();
        for (ResourceId collectionId : formIds) {
            toLoad.add(new FormSchemaKey(collectionId));
        }
        Map<FormSchemaKey, FormSchemaEntity> entityMap = datastore.get(toLoad);
        Map<ResourceId, FormClass> formClassMap = new HashMap<>();

        for (Map.Entry<FormSchemaKey, FormSchemaEntity> entry : entityMap.entrySet()) {
            formClassMap.put(entry.getKey().getCollectionId(), entry.getValue().readFormClass());
        }
        
        return formClassMap;
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId) {
        return Collections.emptyList();
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getForm(resourceId).get().getFormClass();
    }
}
