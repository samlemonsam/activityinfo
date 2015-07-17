package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.shared.model.PartnerDTO;

public class PartnerInstanceAdapter implements Function<PartnerDTO, FormInstance> {

    private final ResourceId classId;

    public PartnerInstanceAdapter(ResourceId formClassId) {
        this.classId = formClassId;
    }

    @Override
    public FormInstance apply(PartnerDTO input) {
        FormInstance instance = new FormInstance(CuidAdapter.partnerInstanceId(input.getId()), classId);

        instance.set(PartnerClassAdapter.getNameField(classId), input.getName());
        instance.set(PartnerClassAdapter.getFullNameField(classId), input.getFullName());
        return instance;
    }
}
