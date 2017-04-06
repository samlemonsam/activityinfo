package org.activityinfo.model.type;


import com.google.gson.JsonElement;

public interface FieldType {

    /**
     * @return the {@code FieldTypeClass} of which this {@code FieldType}
     * is a member
     */
    FieldTypeClass getTypeClass();

    FieldValue parseJsonValue(JsonElement value);

    <T> T accept(FieldTypeVisitor<T> visitor);

    /**
     * @return true if fields of this type can be set by the user.
     */
    boolean isUpdatable();
}
