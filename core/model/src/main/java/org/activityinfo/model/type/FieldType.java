package org.activityinfo.model.type;


import java.io.Serializable;

public interface FieldType extends Serializable {

    /**
     * @return the {@code FieldTypeClass} of which this {@code FieldType}
     * is a member
     */
    FieldTypeClass getTypeClass();


}
