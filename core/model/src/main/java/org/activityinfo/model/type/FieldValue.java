package org.activityinfo.model.type;

/**
 * Instance of a typed field value
 */
public interface FieldValue {

    /**
     * The name of the field that contains the id of the {@code FieldTypeClass}
     * of this value
     */
    public static final String TYPE_CLASS_FIELD_NAME = "@type";

    /**
     *
     * @return this value's {@code FieldTypeClass}
     */
    FieldTypeClass getTypeClass();
}
