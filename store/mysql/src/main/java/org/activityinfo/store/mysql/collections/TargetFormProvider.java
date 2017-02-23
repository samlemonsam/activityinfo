package org.activityinfo.store.mysql.collections;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
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
        return formId.getDomain() == CuidAdapter.TARGET_FORM_CLASS_DOMAIN;
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
    public Optional<ResourceId> lookupForm(QueryExecutor executor, ResourceId recordId) throws SQLException {
        return Optional.absent();
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
