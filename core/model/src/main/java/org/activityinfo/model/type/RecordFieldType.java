package org.activityinfo.model.type;

import org.activityinfo.model.form.FormClass;

/**
 * Marker interface for record-valued types.
 */
public interface RecordFieldType extends FieldType {

    /**
     * 
     * @return the {@link FormClass} that describes the values of this type
     */
    FormClass getFormClass();
}
