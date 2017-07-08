package org.activityinfo.store.query.server;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormPermissions;
import org.activityinfo.store.spi.FormStorage;

import java.util.logging.Logger;

public class FormSupervisorAdapter implements FormSupervisor {

    private static final Logger LOGGER = Logger.getLogger(FormSupervisor.class.getName());

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
            LOGGER.severe("Form " + formId + " does not exist.");
            throw new IllegalStateException("Invalid form ID");
        }
        return form.get().getPermissions(userId);
    }
}
