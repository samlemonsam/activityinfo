package org.activityinfo.service.store;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.Map;

/**
 * Contract for obtaining a {@link ResourceCollection}
 * for a given {@code ResourceId}
 */
public interface CollectionCatalog extends FormClassProvider {

    Optional<ResourceCollection> getCollection(ResourceId collectionId);
    
    Optional<ResourceCollection> lookupCollection(ResourceId resourceId); 

    Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> collectionIds);
}
