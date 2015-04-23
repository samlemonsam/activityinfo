package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;

/**
 * Converts an AdminEntityDTO to a FormInstance
 */
public class AdminEntityInstanceAdapter implements Function<AdminEntityDTO, FormInstance> {


    @Override
    public FormInstance apply(AdminEntityDTO input) {

        ResourceId classId = CuidAdapter.adminLevelFormClass(input.getLevelId());
        ResourceId instanceId = CuidAdapter.entity(input.getId());
        FormInstance instance = new FormInstance(instanceId, classId);

        // Parent field value
        if (input.getParentId() != null) {
            instance.set(CuidAdapter.field(classId, CuidAdapter.ADMIN_PARENT_FIELD),
                    CuidAdapter.entity(input.getParentId()));
        }

        instance.set(CuidAdapter.field(classId, CuidAdapter.NAME_FIELD), input.getName());

        return instance;
    }
}
