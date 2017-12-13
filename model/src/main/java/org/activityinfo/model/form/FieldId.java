package org.activityinfo.model.form;

import org.activityinfo.model.resource.ResourceId;

/**
 * Globally and uniquely identifies the named field of a given {@code FormClass}.
 * Distinguishes, for example, between the {@code label} field of a Province {@code FormClass}
 * and the {@code label} form on another {@code FormClass}.
 *
 * TODO: Properly split use of ResourceIds between real ResourceIds and FieldIds
 */
public class FieldId {

    public static ResourceId fieldId(ResourceId classId, String fieldName) {
        return ResourceId.valueOf(classId.asString() + "$" + fieldName);
    }

    public static ResourceId getFormClassId(ResourceId fieldId) {
        String qfn = fieldId.asString();
        int delimiter = qfn.indexOf('$');
        if(delimiter == -1) {
            throw new IllegalArgumentException("Not a fieldId: " + fieldId);
        }
        return ResourceId.valueOf(qfn.substring(0, delimiter));
    }

    public static String getFieldName(ResourceId fieldId) {
        String qfn = fieldId.asString();
        int delimiter = qfn.indexOf('$');
        if(delimiter == -1) {
            throw new IllegalArgumentException("Not a fieldId: " + fieldId);
        }
        return qfn.substring(delimiter+1);
    }
}
