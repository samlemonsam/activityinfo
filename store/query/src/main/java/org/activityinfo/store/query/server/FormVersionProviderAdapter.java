package org.activityinfo.store.query.server;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.FormVersionProvider;

public class FormVersionProviderAdapter implements FormVersionProvider {
    private final FormCatalog catalog;

    public FormVersionProviderAdapter(FormCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public long getCurrentFormVersion(ResourceId formId) {
        Optional<FormStorage> storage = catalog.getForm(formId);
        if(storage.isPresent()) {
            return storage.get().cacheVersion();
        } else {
            return 0L;
        }
    }
}
