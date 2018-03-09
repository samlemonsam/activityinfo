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

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.DatabaseTargetForm;
import org.activityinfo.store.spi.FormStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;


/**
 * We consider that there is one target form class per database.
 */
public class TargetFormProvider implements FormProvider {
    @Override
    public boolean accept(ResourceId formId) {
        return formId.getDomain() == CuidAdapter.TARGET_FORM_CLASS_DOMAIN &&
                CuidAdapter.isValidLegacyId(formId);
    }

    @Override
    public FormStorage openForm(QueryExecutor executor, ResourceId formId) throws SQLException {
        Map<ResourceId, FormStorage> result = openForms(executor, Collections.singleton(formId));
        FormStorage collection = result.get(formId);
        if(collection == null) {
            throw new IllegalArgumentException("no such target collection: " + formId);
        }
        return collection;
    }

    @Override
    public Map<ResourceId, FormStorage> openForms(QueryExecutor executor, Set<ResourceId> formIds) throws SQLException {
        
        Set<Integer> targetIds = Sets.newHashSet();
        for (ResourceId resourceId : formIds) {
            if(accept(resourceId)) {
                targetIds.add(CuidAdapter.getLegacyIdFromCuid(resourceId));
            }
        }
        
        Map<ResourceId, FormStorage> collectionMap = Maps.newHashMap();

        if(!targetIds.isEmpty()) {
            
            Map<Integer, DatabaseTargetForm> targetMap = Maps.newHashMap();

            try (ResultSet rs = executor.query(
                    "SELECT " +
                        "D.DatabaseId, " +
                        "D.Name, " +
                        "I.IndicatorId, " +
                        "I.Name, " +
                        "I.Units, " +
                        "A.schemaVersion " +
                        " FROM userdatabase D " +
                        " LEFT JOIN activity A ON (D.DatabaseId = A.DatabaseId and A.dateDeleted IS NULL) " +
                        " LEFT JOIN indicator I ON (A.ActivityId=I.ActivityId and I.dateDeleted IS NULL and I.type = 'QUANTITY') " +
                        " WHERE D.databaseID IN (" + Joiner.on(',').join(targetIds) + ")")) {

                while (rs.next()) {
                    int databaseId = rs.getInt(1);
                    DatabaseTargetForm target = targetMap.get(databaseId);
                    if (target == null) {
                        target = new DatabaseTargetForm(databaseId, rs.getString(2), rs.getLong(6));
                        targetMap.put(databaseId, target);
                    }

                    int indicatorId = rs.getInt(3);
                    if(!rs.wasNull()) {
                        target.addIndicator(indicatorId, rs.getString(4), rs.getString(5));
                    }
                }
            }

            for (DatabaseTargetForm target : targetMap.values()) {
                collectionMap.put(target.getFormClassId(), new TargetFormStorage(executor, target));
            }
        }
        
        return collectionMap;
    }
}
