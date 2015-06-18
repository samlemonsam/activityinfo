package org.activityinfo.service.store;

import com.google.common.base.Optional;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.resource.ResourceId;

/**
 * Contract for obtaining a {@link ResourceCollection}
 * for a given {@code Collection}
 */
public interface CollectionCatalog extends FormClassProvider {

    Optional<ResourceCollection> getCollection(ResourceId resourceId);
    
    Optional<ResourceCollection> lookupCollection(ResourceId resourceId); 

}
