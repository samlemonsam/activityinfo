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
package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.spi.FormStorage;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * Internal interface for the MySqlCatalog, which helps to delegate FormStorage implementation
 * based on the domain of the form id. This is in place because different kinds of forms, such as 
 * "sites", "targets", "locations", are stored in very differently-shaped tables.
 */
public interface FormProvider {

    /**
     * True if this provider can handle the given {@code formId}
     */
    boolean accept(ResourceId formId);

    FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException;

    Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException;
}
