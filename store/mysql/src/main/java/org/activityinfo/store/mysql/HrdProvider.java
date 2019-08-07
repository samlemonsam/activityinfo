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
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.HrdStorageProvider;
import org.activityinfo.store.mysql.collections.FormProvider;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.spi.FormNotFoundException;
import org.activityinfo.store.spi.FormStorage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HrdProvider implements FormProvider {
    
    private HrdStorageProvider catalog = new HrdStorageProvider();
    
    @Override
    public boolean accept(ResourceId formId) {
        return formId.getDomain() == 'c';
    }

    @Override
    public FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        Optional<FormStorage> collection = catalog.getForm(formId);
        if(!collection.isPresent()) {
            throw new FormNotFoundException(formId);
        }
        return collection.get();
    }

    @Override
    public Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        Map<ResourceId, FormStorage> map = new HashMap<>();
        for (ResourceId resourceId : formIds) {
            if(accept(resourceId)) {
                try {
                    map.put(resourceId, openForm(executor, resourceId));
                } catch (FormNotFoundException ignored) {
                }
            }
        }
        return map;
    }
}
