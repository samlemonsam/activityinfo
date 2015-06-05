package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.form.FormField;

import java.util.List;

/**
 * Maps a source field to a reference target field using a set of key field pairs.
 * 
 * <p>A "student profile" form might have, for example, a "school" reference field which links the
 * student to the school in which s/he is enrolled.</p>
 * 
 * <p>When importing a student roster from a <em>source</em> CSV file, we will most likely need to "look up"
 * the ActivityInfo id of the student's school using one or more "key" fields in the CSV file, such 
 * as "School name" and "School type". Key fields may also include fields nested in the reference field tree,
 * for example, the name of the province in which the school is located, which might appear as a text field in
 * the source flat file, but as a field on a referenced form in the target.
 * 
 */
public class ReferenceFieldMapping implements FieldMapping {

    /**
     * The id of the field in the <em>target</em> form to which we are mapping
     */
    private final FormField targetReferenceField;
    private final SourceKeySet sourceKeySet;
    private final LookupGraph lookupGraph;

    
    public ReferenceFieldMapping(FormField targetReferenceField, KeyFieldPairSet keyFields) {
        this.targetReferenceField = targetReferenceField;
        this.sourceKeySet = new SourceKeySet(keyFields);
        this.lookupGraph = new LookupGraph(sourceKeySet, keyFields.getTarget());
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
