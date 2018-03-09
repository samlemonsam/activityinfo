/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.collect.Iterables;
import org.activityinfo.geoadmin.merge2.model.ReferenceMatch;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulSet;

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
    private final Observable<LookupTable> lookupTable;


    public ReferenceFieldMapping(
            FormField targetReferenceField, 
            KeyFieldPairSet keyFields, 
            final StatefulSet<ReferenceMatch> referenceMatches) {
        this.targetReferenceField = targetReferenceField;
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
        ResourceId targetId = lookupTable.get().getTargetMatchId(keyIndex);
        ResourceId targetFormId = Iterables.getOnlyElement(((ReferenceType) targetReferenceField.getType()).getRange());
        return new ReferenceValue(new RecordRef(targetFormId, targetId));
    }

    @Override
    public ResourceId getTargetFieldId() {
        return targetReferenceField.getId();
    }
}
