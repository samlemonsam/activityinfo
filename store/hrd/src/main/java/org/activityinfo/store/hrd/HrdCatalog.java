package org.activityinfo.store.hrd;

import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.hrd.op.CreateOrUpdateForm;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;

import java.util.*;

/**
 * Catalog of Collection hosted in the AppEngine High Replication Datastore
 */
public class HrdCatalog implements FormCatalog {

    static {
        ObjectifyService.register(FormEntity.class);
        ObjectifyService.register(FormRecordEntity.class);
        ObjectifyService.register(FormRecordSnapshotEntity.class);
        ObjectifyService.register(FormSchemaEntity.class);
    }
    
    public HrdFormStorage create(FormClass formClass) {
        ObjectifyService.ofy().transact(new CreateOrUpdateForm(formClass));
        
        return new HrdFormStorage(formClass);
    }
    
    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {

        FormSchemaEntity schemaEntity = ObjectifyService.ofy().load().key(FormSchemaEntity.key(formId)).now();
        if(schemaEntity == null) {
            return Optional.absent();
        }

        HrdFormStorage accessor = new HrdFormStorage(schemaEntity.readFormClass());
        
        return Optional.<FormStorage>of(accessor);
    }

    @Override
    public Optional<FormStorage> lookupForm(ResourceId recordId) {
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
        
        Set<Key<FormSchemaEntity>> toLoad = new HashSet<>();
        for (ResourceId formId : formIds) {
            toLoad.add(FormSchemaEntity.key(formId));
        }
        Map<Key<FormSchemaEntity>, FormSchemaEntity> entityMap = ObjectifyService.ofy().load().keys(toLoad);
        
        Map<ResourceId, FormClass> formClassMap = new HashMap<>();
        for (FormSchemaEntity formSchema : entityMap.values()) {
            formClassMap.put(formSchema.getFormId(), formSchema.readFormClass());
        }
        
        return formClassMap;
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        return Collections.emptyList();
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getForm(resourceId).get().getFormClass();
    }
}
