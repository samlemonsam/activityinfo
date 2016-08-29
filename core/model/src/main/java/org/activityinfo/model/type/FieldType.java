package org.activityinfo.model.type;


import com.google.gson.JsonElement;

import java.io.Serializable;

public interface FieldType extends Serializable {

    /**
     * @return the {@code FieldTypeClass} of which this {@code FieldType}
     * is a member
     */
    FieldTypeClass getTypeClass();


    FieldValue parseJsonValue(JsonElement value);
}
