package org.activityinfo.store.query.shared;


import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormPermissions;

public class NullFormSupervisor implements FormSupervisor {

    @Override
    public FormPermissions getFormPermissions(ResourceId formId) {
        return FormPermissions.full();
    }
}
