package org.activityinfo.service.store;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;

/**
 * Internal interface used to construct table queries.
 */
public interface ResourceCollection {

    /**
     * Fetches the permissions which apply to the given user for this collection.
     */
    CollectionPermissions getPermissions(int userId);
    
    /**
     * Retrieves a single resource from the ResourceStore.
     * @param resourceId the id of the resource to retrieve
     * @return the Resource
     */
    Optional<Resource> get(ResourceId resourceId);

    /**
     * @return this collection's schema
     */
    FormClass getFormClass();
    
    /**
     * Adds a new resource to the collection. 
     * 
     * @param update the properties of the new resource
     * @throws java.lang.IllegalStateException if a resource with the given {@code resourceId} already exists              
     */
    void add(ResourceUpdate update);

    /**
     * Updates an existing resource within the collection
     * 
     * @param update the changes to apply to the resource
     * @throws java.lang.IllegalStateException if the resource does not exist, or if the update               
     */
    void update(ResourceUpdate update);

    

    ColumnQueryBuilder newColumnQuery();


}
