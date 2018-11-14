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
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.DatabaseProvider;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.store.spi.FormStorage;

import java.util.logging.Logger;

public class FormSupervisorAdapter implements FormSupervisor {

    private static final Logger LOGGER = Logger.getLogger(FormSupervisor.class.getName());

    private final FormStorageProvider catalog;
    private final DatabaseProvider databaseProvider;
    private int userId;

    public FormSupervisorAdapter(FormStorageProvider catalog, DatabaseProvider databaseProvider, int userId) {
        this.catalog = catalog;
        this.databaseProvider = databaseProvider;
        this.userId = userId;
    }

    @Override
    public FormPermissions getFormPermissions(ResourceId formId) {
        Optional<FormStorage> form = catalog.getForm(formId);
        if(!form.isPresent()) {
            LOGGER.severe("Form " + formId + " does not exist.");
            throw new IllegalStateException("Invalid form ID");
        }
        ResourceId databaseId = form.get().getFormClass().getDatabaseId();
        UserDatabaseMeta databaseMeta = databaseProvider.getDatabaseMetadata(databaseId, userId);
        return PermissionOracle.formPermissions(formId, databaseMeta);
    }
}
