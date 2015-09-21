package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.model.ReferenceMatch;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulSet;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

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
    private final Observable<LookupTable> lookupTable;
    private final ColumnView targetReferenceIds;


    public ReferenceFieldMapping(
            FieldProfile targetReferenceField, 
            KeyFieldPairSet keyFields, 
            final StatefulSet<ReferenceMatch> referenceMatches) {
        this.targetReferenceField = targetReferenceField.getFormField();
        this.targetReferenceIds = targetReferenceField.getView();
        this.sourceKeySet = new SourceKeySet(keyFields);
        this.lookupGraph = new LookupGraph(sourceKeySet, keyFields.getTarget());
        this.lookupTable = LookupTable.compute(lookupGraph, referenceMatches);
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

    public Observable<LookupTable> getLookupTable() {
        return lookupTable;
    }

    public SourceKeySet getSourceKeySet() {
        return sourceKeySet;
    }
    
    public FieldValue mapFieldValue(int sourceIndex) {
        int keyIndex = sourceKeySet.getKeyIndexOfSourceRow(sourceIndex);
        int targetIndex = lookupTable.get().getTargetMatchRow(keyIndex);
        ResourceId targetId = ResourceId.valueOf(targetReferenceIds.getString(targetIndex));
        return new ReferenceValue(targetId);
    }

    @Override
    public ResourceId getTargetFieldId() {
        return targetReferenceField.getId();
    }
}
