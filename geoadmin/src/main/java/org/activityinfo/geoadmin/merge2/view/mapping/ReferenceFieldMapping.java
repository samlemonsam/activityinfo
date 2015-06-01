package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.geoadmin.merge2.view.match.FieldMatching;
import org.activityinfo.model.form.FormField;

/**
 * Maps a source field to a target field
 */
public class ReferenceFieldMapping implements FieldMapping {

    /**
     * The id of the field in the <em>target</em> form to which we are mapping
     */
    private FormField targetField;
    private FieldMatching fieldMatching;

    public ReferenceFieldMapping(FormField targetField, FieldMatching fieldMatching) {
        this.targetField = targetField;
        this.fieldMatching = fieldMatching;
    }
    
    public String getTargetFieldLabel() {
        return targetField.getLabel();
    }
}
