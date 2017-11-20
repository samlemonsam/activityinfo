package org.activityinfo.store.query.shared;


import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.form.FormPermissions;

public class NullFormSupervisor implements FormSupervisor {

    @Override
    public FormPermissions getFormPermissions(ResourceId formId) {
        return FormPermissions.full();
    }
}
