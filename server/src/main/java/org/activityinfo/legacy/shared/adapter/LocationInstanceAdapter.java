package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceValue;

import java.util.List;
import java.util.Set;


/**
 * Converts a legacy LocationDTO to a FormInstance
 */
public class LocationInstanceAdapter implements Function<LocationDTO, FormInstance> {


    @Override
    public FormInstance apply(LocationDTO input) {
        ResourceId instanceId = CuidAdapter.locationInstanceId(input.getId());
        ResourceId classId = CuidAdapter.locationFormClass(input.getLocationTypeId());

        FormInstance instance = new FormInstance(instanceId, classId);
        instance.set(LocationClassAdapter.getNameFieldId(classId), input.getName());
        instance.set(LocationClassAdapter.getAxeFieldId(classId), input.getAxe());
        instance.set(LocationClassAdapter.getPointFieldId(classId), input.getPoint());
        instance.set(LocationClassAdapter.getAdminFieldId(classId), toInstanceIdSet(input.getAdminEntities()));

        return instance;
    }


    private ReferenceValue toInstanceIdSet(List<AdminEntityDTO> adminEntities) {
        // in the legacy database, all admin entities are stored to simplify
        // querying the sql database. here we need to eliminate the redundancy

        Set<Integer> parents = Sets.newHashSet();
        for (AdminEntityDTO entity : adminEntities) {
            if (entity.getParentId() != null) {
                parents.add(entity.getParentId());
            }
        }

        Set<ResourceId> instanceIds = Sets.newHashSet();
        for (AdminEntityDTO entity : adminEntities) {
            if (!parents.contains(entity.getId())) {
                instanceIds.add(CuidAdapter.entity(entity.getId()));
            }
        }
        return new ReferenceValue(instanceIds);
    }
}
