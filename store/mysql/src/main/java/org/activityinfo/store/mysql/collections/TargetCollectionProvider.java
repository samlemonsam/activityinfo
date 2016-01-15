package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.DatabaseTargetForm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;


/**
 * We consider that there is one target form class per database.
 */
public class TargetCollectionProvider implements CollectionProvider {
    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.TARGET_FORM_CLASS_DOMAIN;
    }

    @Override
    public ResourceCollection openCollection(QueryExecutor executor, ResourceId formClassId) throws SQLException {
        Map<ResourceId, ResourceCollection> result = openCollections(executor, Collections.singleton(formClassId));
        return Preconditions.checkNotNull(result.get(formClassId));
    }

    @Override
    public Optional<ResourceId> lookupCollection(QueryExecutor executor, ResourceId resourceId) throws SQLException {
        return Optional.absent();
    }

    @Override
    public Map<ResourceId, ResourceCollection> openCollections(QueryExecutor executor, Set<ResourceId> resourceIds) throws SQLException {
        
        Set<Integer> targetIds = Sets.newHashSet();
        for (ResourceId resourceId : resourceIds) {
            if(accept(resourceId)) {
                targetIds.add(CuidAdapter.getLegacyIdFromCuid(resourceId));
            }
        }
        
        Map<ResourceId, ResourceCollection> collectionMap = Maps.newHashMap();

        if(!targetIds.isEmpty()) {
            
            Map<Integer, DatabaseTargetForm> targetMap = Maps.newHashMap();

            try (ResultSet rs = executor.query(
                    "SELECT " +
                            "D.DatabaseId, " +
                            "D.Name, " +
                            "I.IndicatorId, " +
                            "I.Name, " +
                            "I.Units " +
                            " FROM userdatabase D " +
                            " LEFT JOIN activity A ON (D.DatabaseId = A.DatabaseId) " +
                            " LEFT JOIN indicator I ON (A.ActivityId=I.ActivityId) " +
                            " WHERE D.dateDeleted IS NULL AND " +
                            "   A.dateDeleted IS NULL AND " +
                            "   I.dateDeleted IS NULL AND " +
                            "   I.Type = 'QUANTITY'")) {

                while (rs.next()) {
                    int databaseId = rs.getInt(1);
                    DatabaseTargetForm target = targetMap.get(databaseId);
                    if (target == null) {
                        target = new DatabaseTargetForm(databaseId, rs.getString(2));
                        targetMap.put(databaseId, target);
                    }

                    target.addIndicator(rs.getInt(3), rs.getString(4), rs.getString(5));
                }
            }

            for (DatabaseTargetForm target : targetMap.values()) {
                collectionMap.put(target.getFormClassId(), new TargetCollection(executor, target));
            }
        }
        
        return collectionMap;
    }
}
