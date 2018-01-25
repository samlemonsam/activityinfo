package org.activityinfo.model.type;


import org.activityinfo.json.JsonValue;

public interface FieldType {

    /**
     * @return the {@code FieldTypeClass} of which this {@code FieldType}
     * is a member
     */
    FieldTypeClass getTypeClass();

    FieldValue parseJsonValue(JsonValue value);

    <T> T accept(FieldTypeVisitor<T> visitor);

    /**
     * @return true if fields of this type can be set by the user.
     */
    boolean isUpdatable();
}
