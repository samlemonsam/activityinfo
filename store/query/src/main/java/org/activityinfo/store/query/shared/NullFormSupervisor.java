package org.activityinfo.store.query.shared;


import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.resource.ResourceId;

public class NullFormSupervisor implements FormSupervisor {

    @Override
    public FormPermissions getFormPermissions(ResourceId formId) {
        return FormPermissions.readWrite();
    }
}
