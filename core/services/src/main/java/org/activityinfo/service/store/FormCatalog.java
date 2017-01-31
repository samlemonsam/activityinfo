package org.activityinfo.service.store;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.formTree.BatchFormClassProvider;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * Contract for obtaining a {@link FormStorage}
 * for a given {@code formId}
 */
public interface FormCatalog extends FormClassProvider, BatchFormClassProvider {

    Optional<FormStorage> getForm(ResourceId formId);
    
    Optional<FormStorage> lookupForm(ResourceId recordId);

    List<CatalogEntry> getRootEntries();
    
    List<CatalogEntry> getChildren(String parentId, int userId);
}
