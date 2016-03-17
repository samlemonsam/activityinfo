package org.activityinfo.service.store;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

import java.io.IOException;


/**
 * Builds a query that returns the requested fields as column streams.
 */
public interface ColumnQueryBuilder {

    void only(ResourceId resourceId);
    
    /**
     * Adds the {@code resourceId} to the list of columns to fetch.
     * 
     * @param observer interface to the object that will receive the stream
     *                 of {@code resourceIds} when {@link #execute()} is called.                 
     */
    void addResourceId(CursorObserver<ResourceId> observer);

    /**
     * Adds the {@code fieldId} to the list of columns to fetch.
     * * 
     * @param fieldId the id of the column to fetch
     * @param observer interface to the object that will receive the stream
     *                 of column values when {@link #execute()} is called.                 
     */
    void addField(ResourceId fieldId, CursorObserver<FieldValue> observer);

    /**
     * Fetches the requested columns.
     */
    void execute() throws IOException;
}
