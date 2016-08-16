package org.activityinfo.service.store;

import com.google.common.base.Optional;
import org.activityinfo.api.client.FormHistoryEntryBuilder;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * Internal interface used to construct table queries.
 */
public interface FormAccessor {

    /**
     * Fetches the permissions which apply to the given user for this collection.
     */
    FormPermissions getPermissions(int userId);
    
    /**
     * Retrieves a single resource from the ResourceStore.
     * @param resourceId the id of the resource to retrieve
     * @return the Resource
     */
    Optional<FormRecord> get(ResourceId resourceId);
    
    List<FormHistoryEntryBuilder> getHistory(ResourceId resourceId);
    
    /**
     * @return this collection's schema
     */
    FormClass getFormClass();

    
    
    void updateFormClass(FormClass formClass);
    
    /**
     * Adds a new resource to the collection. 
     * 
     * @param update the properties of the new resource
     * @throws java.lang.IllegalStateException if a resource with the given {@code resourceId} already exists              
     */
    void add(RecordUpdate update);
    
    /**
     * Updates an existing resource within the collection
     * 
     * @param update the changes to apply to the resource
     * @throws java.lang.IllegalStateException if the resource does not exist, or if the update               
     */
    void update(RecordUpdate update);

    

    ColumnQueryBuilder newColumnQuery();


    /**
     * 
     * @return the current version of this collection to lookup.
     */
    long cacheVersion();

    
}
