package org.activityinfo.service.store;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;

/**
 * Internal interface used to construct table queries.
 */
public interface CollectionAccessor {

    /**
     * Retrieves a single resource from the ResourceStore.
     * @param resourceId the id of the resource to retrieve
     * @return the Resource
     * @throws
     */
    Resource get(ResourceId resourceId);

    /**
     *
     * @return this collection's schema
     */
    FormClass getFormClass();


    CursorBuilder newCursor();

    /**
     * Opens a cursor over a list of ALL instances belong to a FormClass.
     * Authorization is NOT applied to this cursor, the table builder should
     * add the authorization expression as part of the filter.
     */
    Cursor openCursor(ResourceId formClassId) throws Exception;


}
