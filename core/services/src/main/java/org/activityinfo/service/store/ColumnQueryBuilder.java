package org.activityinfo.service.store;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

import java.sql.SQLException;

/**
 * Constructs a column query batch
 */
public interface ColumnQueryBuilder {
    
    void addResourceId(CursorObserver<ResourceId> observer);
    
    void addField(ResourceId fieldId, CursorObserver<FieldValue> observer);
    
    void execute() throws SQLException;
}
