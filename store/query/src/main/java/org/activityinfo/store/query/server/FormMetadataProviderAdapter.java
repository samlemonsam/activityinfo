package org.activityinfo.store.query.server;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.formTree.FormMetadataProvider;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;

class FormMetadataProviderAdapter implements FormMetadataProvider {

    private FormCatalog catalog;
    private FormSupervisor supervisor;

    public FormMetadataProviderAdapter(FormCatalog catalog, FormSupervisor supervisor) {
        this.catalog = catalog;
        this.supervisor = supervisor;
    }

    @Override
    public FormMetadata getFormMetadata(ResourceId formId) {
        Optional<FormStorage> storage = catalog.getForm(formId);
        if(storage.isPresent()) {
            FormPermissions permissions = supervisor.getFormPermissions(formId);
            if(permissions.isVisible()) {
                return FormMetadata.of(storage.get().cacheVersion(), storage.get().getFormClass(), permissions);
            } else {
                return FormMetadata.forbidden(formId);
            }
        } else {
            return FormMetadata.notFound(formId);
        }
    }
}
