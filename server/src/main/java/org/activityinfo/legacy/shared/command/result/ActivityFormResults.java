package org.activityinfo.legacy.shared.command.result;

import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.AttributeDTO;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;

import java.util.List;

public class ActivityFormResults extends ListResult<ActivityFormDTO> {

    public ActivityFormResults() {
    }

    public ActivityFormResults(List<ActivityFormDTO> data) {
        super(data);
    }

    /**
     * Retrieves the name of the AttributeGroup from the Schema graph,
     * or returns "" if the attribute group cannot be found in the
     * loaded schema.
     */
    public String getAttributeGroupNameSafe(int attributeGroupId) {
        AttributeGroupDTO group = getAttributeGroupById(attributeGroupId);
        if (group == null) {
            return "";
        } else {
            return group.getName();
        }
    }


    public AttributeGroupDTO getAttributeGroupById(int attributeGroupId) {
        for (ActivityFormDTO activity : getData()) {
            AttributeGroupDTO group = activity.getAttributeGroupById(attributeGroupId);
            if (group != null) {
                return group;
            }
        }
        return null;
    }
}
