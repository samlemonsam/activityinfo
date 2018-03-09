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

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.PermissionsCache;
import org.activityinfo.store.spi.FormStorage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class LocationFormProvider implements FormProvider {

    private final PermissionsCache permissionsCache;

    public LocationFormProvider(PermissionsCache permissionsCache) {
        this.permissionsCache = permissionsCache;
    }


    @Override
    public boolean accept(ResourceId formId) {
        return formId.getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN &&
                CuidAdapter.isValidLegacyId(formId);
    }

    @Override
    public FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        return new LocationFormStorage(executor, formId, permissionsCache);
    }

    @Override
    public Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        Map<ResourceId, FormStorage> result = new HashMap<>();
        for (ResourceId collectionId : formIds) {
            if(accept(collectionId)) {
                result.put(collectionId, openForm(executor, collectionId));
            }
        }
        return result;
    }

}
