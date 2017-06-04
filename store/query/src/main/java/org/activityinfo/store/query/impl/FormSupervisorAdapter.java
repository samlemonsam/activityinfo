package org.activityinfo.store.query.impl;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormPermissions;
import org.activityinfo.store.spi.FormStorage;

public class FormSupervisorAdapter implements FormSupervisor {

    private final FormCatalog catalog;
    private int userId;

    public FormSupervisorAdapter(FormCatalog catalog, int userId) {
        this.catalog = catalog;
        this.userId = userId;
    }

    @Override
    public FormPermissions getFormPermissions(ResourceId formId) {
        Optional<FormStorage> form = catalog.getForm(formId);
        if(!form.isPresent()) {
            return FormPermissions.none();
        }
        return form.get().getPermissions(userId);
    }
}
