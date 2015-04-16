package org.activityinfo.service.store;

import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.resource.ResourceId;

/**
 * Contract for obtaining a {@link CollectionAccessor}
 * for a given {@code Collection}
 */
public interface CollectionCatalog extends FormClassProvider {

    CollectionAccessor getCollection(ResourceId resourceId);

}
