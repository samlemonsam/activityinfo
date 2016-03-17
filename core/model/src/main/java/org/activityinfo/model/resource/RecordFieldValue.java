package org.activityinfo.model.resource;


import org.activityinfo.model.type.FieldValue;

public interface RecordFieldValue extends FieldValue {
    
    FieldValue getField(String id);
}
