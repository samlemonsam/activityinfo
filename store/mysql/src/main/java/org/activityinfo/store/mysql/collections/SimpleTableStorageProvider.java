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

import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.SimpleTable;
import org.activityinfo.store.spi.FormStorage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Provides storage for forms that are stored in a simple table, where each field maps more or less
 * to one table column.
 */
public class SimpleTableStorageProvider implements FormProvider {
    
    protected final SimpleTable table;

    public SimpleTableStorageProvider(SimpleTable table) {
        this.table = table;
    }

    @Override
    public boolean accept(ResourceId formId) {
        return table.accept(formId);
    }

    @Override
    public FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        return new SimpleTableStorage(table.getMapping(executor, formId), executor);
    }

    @Override
    public Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        Map<ResourceId, FormStorage> map = new HashMap<>();
        for (ResourceId collectionId : formIds) {
            if(table.accept(collectionId)) {
                map.put(collectionId, openForm(executor, collectionId));
            }
        }
        return map;
    }

}
