package org.activityinfo.store.hrd;

import org.activityinfo.model.type.FieldValue;

/**
 * Converts between a datastore property and an ActivityInfo field value
 */
public interface FieldConverter<FieldT extends FieldValue> {
    
    Object toHrdProperty(FieldT value);
    
    FieldT toFieldValue(Object hrdValue);
    
}
