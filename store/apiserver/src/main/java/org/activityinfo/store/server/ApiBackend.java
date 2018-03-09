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
package org.activityinfo.store.server;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.PermissionsEnforcer;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.RecordHistoryProvider;

/**
 * Temporary interface to split out dependencies on ActivityInfo
 */
public interface ApiBackend {

    FormStorageProvider getStorage();

    FormCatalog getCatalog();

    FormSupervisor getFormSupervisor();

    int getAuthenticatedUserId();

    void createNewForm(FormClass formClass);

    Updater newUpdater();

    ColumnSetBuilder newQueryBuilder();

    PermissionsEnforcer newPermissionsEnforcer();

    RecordHistoryProvider getRecordHistoryProvider();

}
