package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoArea;


public class GeoAreaFieldMapping implements FieldMapping {

    private FieldProfile sourceField;
    private FormField targetField;

    public GeoAreaFieldMapping(FieldProfile sourceField, FormField targetField) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }

    @Override
    public ResourceId getTargetFieldId() {
        return targetField.getId();
    }

    @Override
    public FieldValue mapFieldValue(int sourceIndex) {
        return new GeoArea(sourceField.getExtents(sourceIndex));
    }
}
