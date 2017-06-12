package org.activityinfo.store.spi;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * Internal interface used to construct table queries.
 */
public interface FormStorage {

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

    /**
     * Retrieves a list of versions of this record.
     */
    List<RecordVersion> getVersions(ResourceId recordId);
    
    List<RecordVersion> getVersionsForParent(ResourceId parentRecordId);


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

    /**
     * Update the geometry associated with a specific record and field.
     */
    void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value);

}
