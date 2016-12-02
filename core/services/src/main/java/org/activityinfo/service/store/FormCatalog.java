package org.activityinfo.service.store;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.BatchFormClassProvider;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Contract for obtaining a {@link FormAccessor}
 * for a given {@code formId}
 */
public interface FormCatalog extends FormClassProvider, BatchFormClassProvider {

    Optional<FormAccessor> getForm(ResourceId formId);
    
    Optional<FormAccessor> lookupForm(ResourceId recordId); 


    List<CatalogEntry> getRootEntries();
    
    List<CatalogEntry> getChildren(String parentId, int userId);
}
