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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.DatabaseProvider;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.model.permission.FormPermissions;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FormSupervisorAdapter implements FormSupervisor {

    private static final Logger LOGGER = Logger.getLogger(FormSupervisor.class.getName());

    private final FormStorageProvider catalog;
    private final DatabaseProvider databaseProvider;
    private final int userId;

    private final Map<ResourceId,FormPermissions> formPermissionsCache = Maps.newHashMap();

    public FormSupervisorAdapter(FormStorageProvider catalog, DatabaseProvider databaseProvider, int userId) {
        this.catalog = catalog;
        this.databaseProvider = databaseProvider;
        this.userId = userId;
    }

    @Override
    public FormPermissions getFormPermissions(ResourceId formId) {
        return getFormPermissions(Collections.singleton(formId)).get(formId);
    }

    @Override
    public Map<ResourceId,FormPermissions> getFormPermissions(Set<ResourceId> formIds) {
        if (formIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ResourceId,FormPermissions> permissions = new HashMap<>(formIds.size());

        Set<ResourceId> cached = formIds.stream().filter(formPermissionsCache::containsKey).collect(Collectors.toSet());
        cached.forEach(cachedForm -> permissions.put(cachedForm, formPermissionsCache.get(cachedForm)));

        Set<ResourceId> toFetch = Sets.difference(formIds, cached).immutableCopy();
        Map<ResourceId,FormClass> forms = catalog.getFormClasses(toFetch);
        Map<ResourceId,ResourceId> formDbMap = forms.values().stream()
                .collect(Collectors.toMap(
                        FormClass::getId,
                        FormClass::getDatabaseId));
        Map<ResourceId,UserDatabaseMeta> dbs = databaseProvider.getDatabaseMetadata(Sets.newHashSet(formDbMap.values()), userId);
        Map<ResourceId,FormPermissions> fetchedPermissions = formDbMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        formDbEntry -> {
                            if (!dbs.containsKey(formDbEntry.getValue())) {
                                return FormPermissions.none();
                            }
                            return PermissionOracle.formPermissions(formDbEntry.getKey(), dbs.get(formDbEntry.getValue()));
                        }));

        formPermissionsCache.putAll(fetchedPermissions);
        permissions.putAll(fetchedPermissions);
        return permissions;
    }

}
