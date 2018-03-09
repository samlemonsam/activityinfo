/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.FormStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormFolder {
    private FormStorageProvider catalog;

    public FormFolder(FormStorageProvider catalog) {
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
