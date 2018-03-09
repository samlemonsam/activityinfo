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
package org.activityinfo.store.query.server;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.formTree.FormMetadataProvider;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.FormStorage;

class FormMetadataProviderAdapter implements FormMetadataProvider {

    private FormStorageProvider catalog;
    private FormSupervisor supervisor;

    public FormMetadataProviderAdapter(FormStorageProvider catalog, FormSupervisor supervisor) {
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
