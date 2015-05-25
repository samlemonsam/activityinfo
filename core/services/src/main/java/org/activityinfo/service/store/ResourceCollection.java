package org.activityinfo.service.store;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;

/**
 * Internal interface used to construct table queries.
 */
public interface ResourceCollection {

    /**
     * Retrieves a single resource from the ResourceStore.
     * @param resourceId the id of the resource to retrieve
     * @return the Resource
     */
    Resource get(ResourceId resourceId);

    /**
     *
     * @return this collection's schema
     */
    FormClass getFormClass();


    ColumnQueryBuilder newColumnQuery();


}
