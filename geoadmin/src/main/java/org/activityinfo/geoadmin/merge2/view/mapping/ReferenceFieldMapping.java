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
    private final FormField targetReferenceField;
    private final FieldMatching fieldMatching;
    private final SourceKeySet sourceKeySet;
    private final LookupTable lookupTable;

    
    public ReferenceFieldMapping(FormField targetReferenceField, FieldMatching fieldMatching) {
        this.targetReferenceField = targetReferenceField;
        this.fieldMatching = fieldMatching;
        this.sourceKeySet = new SourceKeySet(fieldMatching);
        this.lookupTable = new LookupTable(sourceKeySet, fieldMatching);
    }
    
    public String getTargetFieldLabel() {
        return targetReferenceField.getLabel();
    }

    public LookupTable getLookupTable() {
        return lookupTable;
    }
}
