package org.activityinfo.store.mysql.side;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.CountryStructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Scans the location / adminlink values for a given location type and emits
 * a ReferenceValue for each location.
 */
public class AdminColumnBuilder {
    
    private static final Logger LOGGER = Logger.getLogger(AdminColumnBuilder.class.getName());

    private static final String NEW_LINE = "\n";

    private final int locationTypeId;
    private CountryStructure country;
    private List<CursorObserver<FieldValue>> observers = new ArrayList<>();

    private final int[] adminLevels;
    private final ResourceId[] adminLevelFormIds;
    private final int[] adminParentMap;

    private ResourceId locationId;

    public AdminColumnBuilder(int locationTypeId, CountryStructure country) {
        this.locationTypeId = locationTypeId;
        this.country = country;
        this.adminLevels = country.getAdminLevelIdArray();
        this.adminLevelFormIds = new ResourceId[adminLevels.length];
        for (int i = 0; i < adminLevels.length; i++) {
            adminLevelFormIds[i] = CuidAdapter.adminLevelFormClass(adminLevels[i]);
        }
        this.adminParentMap = country.buildParentIndexMap(adminLevels);
    }


    public void only(ResourceId locationId) {
        this.locationId = locationId;
    }

    public void addObserver(CursorObserver<FieldValue> observer) {
        observers.add(observer);
    }
        
    public boolean hasObservers() {
        return !observers.isEmpty();
    }

    public void execute(QueryExecutor executor) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT location.locationid, link.adminEntityId, link.adminLevelId").append(NEW_LINE);
        sql.append("FROM location").append(NEW_LINE);
        sql.append("LEFT JOIN locationadminlink link ON (location.locationid = link.locationId)").append(NEW_LINE);
        sql.append("WHERE location.locationTypeId=").append(locationTypeId).append(NEW_LINE);
        sql.append(" AND location.workflowStatusId='validated'").append(NEW_LINE);

        if(locationId != null) {
            sql.append(" AND location.locationId=").append(CuidAdapter.getLegacyIdFromCuid(locationId)).append(NEW_LINE);
        }

        LOGGER.info("Querying admin table: " + sql.toString());
        
        int currentLocationId = -1;
        int[] adminEntity = new int[adminLevels.length];

        try(ResultSet rs = executor.query(sql.toString())) {
            while(rs.next()) {
                int locationId = rs.getInt(1);
                if(locationId != currentLocationId) {
                    if(currentLocationId != -1) {
                        emit(adminEntity);
                    }
                    currentLocationId = locationId;
                    Arrays.fill(adminEntity, 0);
                }
                int entityId = rs.getInt(2);
                if(!rs.wasNull()) {
                    int levelId = rs.getInt(3);
                    int levelIndex = Arrays.binarySearch(adminLevels, levelId);
                    if(levelIndex >= 0) {
                        adminEntity[levelIndex] = entityId;
                    }
                }
            }
            if(currentLocationId != -1) {
                emit(adminEntity);
            }
            done();
        }
    }

    private void emit(int[] adminEntity) {
        // From the beginning, AI has always stored *all* admin level members in the 
        // locationadminlink table to make it easier to query
        // This denormalized form, however, is not what we want with the new model,
        // so we need to eliminate the redundant information

        boolean nonNull = false;

        for (int i = 0; i < adminEntity.length; i++) {
            if(adminEntity[i] != 0) {
                nonNull = true;
                int parentIndex = adminParentMap[i];
                if(parentIndex != -1) {
                    // remove the parent from the result -- it is redundant information
                    adminEntity[parentIndex] = 0;
                }
            }
        }

        // Now emit the field value
        if(nonNull) {
            Set<RecordRef> entityIds = new HashSet<>();
            for (int i = 0; i < adminEntity.length; i++) {
                int entityId = adminEntity[i];
                if(entityId != 0) {
                    entityIds.add(new RecordRef(adminLevelFormIds[i], CuidAdapter.entity(entityId)));
                }
            }
            emit(new ReferenceValue(entityIds));
        } else {
            emit(ReferenceValue.EMPTY);
        }
    }

    private void emit(ReferenceValue referenceValue) {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).onNext(referenceValue);
        }
    }

    private void done() {
        for (CursorObserver<FieldValue> observer : observers) {
            observer.done();
        }
    }

}
