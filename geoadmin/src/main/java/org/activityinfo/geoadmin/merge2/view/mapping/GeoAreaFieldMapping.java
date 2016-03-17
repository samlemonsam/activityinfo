package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Preconditions;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoArea;


public class GeoAreaFieldMapping implements FieldMapping {
    
    private ResourceId targetFieldId;
    private ColumnView sourceView;

    public GeoAreaFieldMapping(FieldProfile sourceField, FieldProfile targetField) {
        this.targetFieldId = targetField.getId();
        this.sourceView = Preconditions.checkNotNull(sourceField.getView());
    }

    @Override
    public ResourceId getTargetFieldId() {
        return targetFieldId;
    }

    @Override
    public FieldValue mapFieldValue(int sourceIndex) {
        Extents extents = sourceView.getExtents(sourceIndex);
        if(extents == null) {
            return null;
        } else {
            return new GeoArea(extents);
        }
    }
}
