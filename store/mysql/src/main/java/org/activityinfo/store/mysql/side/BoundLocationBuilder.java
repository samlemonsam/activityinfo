package org.activityinfo.store.mysql.side;


import com.google.common.collect.Lists;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BoundLocationBuilder {
    
    private int activityId;
    private Integer siteId;

    private List<CursorObserver<FieldValue>> observers = Lists.newArrayList();

    public BoundLocationBuilder(int activityId) {
        this.activityId = activityId;
    }

    public void addObserver(CursorObserver<FieldValue> observer) {
        observers.add(observer);
    }

    public boolean hasObservers() {
        return observers.size() > 0;
    }


    public void only(ResourceId siteId) {
        this.siteId = CuidAdapter.getLegacyIdFromCuid(siteId);
    }

    public void execute(QueryExecutor executor) throws SQLException {
        String sql = "SELECT s.siteId,  t.boundAdminLevelId, s.locationId, e.adminEntityId, l.locationTypeId " +
                "FROM site s " +
                "LEFT JOIN location l ON (s.locationId = l.locationId) " +
                "LEFT JOIN locationtype t ON (l.locationTypeId = t.locationTypeId) " +
                "LEFT JOIN locationadminlink k ON (l.locationId = k.locationid) " + 
                "LEFT JOIN adminentity e " +
                        "ON (k.adminEntityId=e.adminEntityId AND (e.adminLevelId = t.boundAdminLevelId)) " +
                "WHERE s.activityId = " + activityId;

        if(siteId != null) {
            sql += " AND s.siteId=" + siteId;
        }

        sql +=  " ORDER BY s.siteId";
        
        System.out.println(sql);
        
        int lastSiteId = -1;
        try(ResultSet rs = executor.query(sql)) {
            while(rs.next()) {
                int siteId = rs.getInt(1);
                int boundLevelId = rs.getInt(2);
                if(rs.wasNull()) {
                    // "Normal" location type. Use only first row
                    // There will be duplicates
                    if (siteId != lastSiteId) {
                        int locationId = rs.getInt(3);
                        emit(new ReferenceValue(
                                new RecordRef(
                                    CuidAdapter.locationFormClass(rs.getInt(5)),
                                    CuidAdapter.locationInstanceId(locationId))));
                    }
                } else {
                    // Bound admin level id 
                    int entityId = rs.getInt(4);
                    if(!rs.wasNull()) {
                        emit(new ReferenceValue(
                                new RecordRef(
                                    CuidAdapter.adminLevelFormClass(boundLevelId),
                                    CuidAdapter.entity(entityId))));
                    }
                }
                lastSiteId = siteId;
            }
        }

        for (CursorObserver<FieldValue> observer : observers) {
            observer.done();
        }
    }

    private void emit(ReferenceValue referenceValue) {
        for (CursorObserver<FieldValue> observer : observers) {
            observer.onNext(referenceValue);
        }
    }

}
