package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormFolder {
    private FormCatalog catalog;

    public FormFolder(FormCatalog catalog) {
        this.catalog = catalog;
    }

    public List<CatalogEntry> getChildren(ResourceId formId) {
        Optional<FormStorage> storage = catalog.getForm(formId);
        if(!storage.isPresent()) {
            return Collections.emptyList();
        }
        List<CatalogEntry> entries = new ArrayList<>();
        FormClass formClass = storage.get().getFormClass();
        for (FormField formField : formClass.getFields()) {
            if(formField.getType() instanceof SubFormReferenceType) {
                SubFormReferenceType subFormType = (SubFormReferenceType) formField.getType();
                ResourceId subFormId = subFormType.getClassId();
                CatalogEntry catalogEntry = new CatalogEntry(subFormId.asString(), formField.getLabel(), CatalogEntryType.FORM);
                catalogEntry.setLeaf(true);

                entries.add(catalogEntry);
            }
        }
        return entries;
    }
}
