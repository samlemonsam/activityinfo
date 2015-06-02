package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.geoadmin.merge2.view.match.FieldMatching;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.form.FormField;

import java.util.List;

/**
 * Maps a source field to a target field
 */
public class ReferenceFieldMapping implements FieldMapping {

    /**
     * The id of the field in the <em>target</em> form to which we are mapping
     */
    private final FormField targetReferenceField;
    private final SourceKeySet sourceKeySet;
    private final LookupGraph lookupGraph;

    
    public ReferenceFieldMapping(FormField targetReferenceField, FieldMatching fieldMatching) {
        this.targetReferenceField = targetReferenceField;
        this.sourceKeySet = new SourceKeySet(fieldMatching);
        this.lookupGraph = new LookupGraph(sourceKeySet, fieldMatching.getTarget());
    }
    
    public List<FieldProfile> getSourceKeyFields() {
        return sourceKeySet.getSourceFields();
    }
    
    public List<FieldProfile> getTargetKeyFields() {
        return sourceKeySet.getTargetFields();
    }

    public String getTargetFieldLabel() {
        return targetReferenceField.getLabel();
    }

    public LookupGraph getLookupGraph() {
        return lookupGraph;
    }

    public SourceKeySet getSourceKeySet() {
        return sourceKeySet;
    }
}
