package org.activityinfo.store.mysql.side;


import com.google.common.collect.Lists;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.spi.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class BoundLocationBuilder {

    private static final Logger LOGGER = Logger.getLogger(BoundLocationBuilder.class.getName());

    private Integer siteId;

    private List<CursorObserver<FieldValue>> observers = Lists.newArrayList();
    private Activity activity;
    private final ResourceId locationFieldId;
    private Set<Integer> boundAdminLevelIds = new HashSet<>();

    public BoundLocationBuilder(Activity activity) {
        this.activity = activity;
        this.locationFieldId = CuidAdapter.locationField(activity.getId());

        for (ResourceId locationFormId : activity.getLocationFormClassIds()) {
            if(locationFormId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN) {
                boundAdminLevelIds.add(CuidAdapter.getLegacyIdFromCuid(locationFormId));
            }
        }
    }

    public boolean hasBoundLocationTypes() {
        return !boundAdminLevelIds.isEmpty();
    }

    /**
     * True if this query builder should handle the given {@code fieldId}
     */
    public boolean accept(ResourceId fieldId) {
        return fieldId.equals(locationFieldId) &&
                (hasBoundLocationTypes() || activity.getLocationFormClassIds().size() > 1);
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

        if(activity.getAdminLevelId() != null) {
            executeSimpleQuery(executor);

        } else {
            executeMixedQuery(executor);
        }
    }

    /**
     * Maps each location reference to the corresponding admin entity reference. We only look at the
     * one admin level that is set as the current location type of the activity.
     *
     * If there are references to old location types, we just use the locationadminlink table to map
     * them to a reference in the new bound admin level.
     */
    private void executeSimpleQuery(QueryExecutor executor) throws SQLException {
        ResourceId referenceFormId = CuidAdapter.adminLevelFormClass(activity.getAdminLevelId());

        String sql = "SELECT s.siteId, k.adminEntityId " +
                "FROM site s " +
                "LEFT JOIN locationadminlink k ON (s.locationId = k.locationId AND k.adminLevelId = " + activity.getAdminLevelId() + ") " +
                "WHERE s.deleted = 0 AND s.activityId = " + activity.getId();

        if(siteId != null) {
            sql += " AND s.siteId=" + siteId;
        }

        sql +=  " ORDER BY s.siteId";

        System.out.println(sql);

        int lastSiteId = -1;
        try(ResultSet rs = executor.query(sql)) {
            while(rs.next()) {
                int siteId = rs.getInt(1);
                if(siteId != lastSiteId) {
                    int adminEntityId = rs.getInt(2);
                    if(rs.wasNull()) {
                        emit(null);
                    } else {
                        emit(new ReferenceValue(
                                new RecordRef(
                                        referenceFormId,
                                        CuidAdapter.entity(adminEntityId))));
                    }
                }
                lastSiteId = siteId;
            }
        }

        for (CursorObserver<FieldValue> observer : observers) {
            observer.done();
        }
    }

    /**
     * Generates a list of references for a location field that references a mix of bound location types
     * and non-bound location types.
     */
    private void executeMixedQuery(QueryExecutor executor) throws SQLException {

        String sql = "SELECT " +
                "s.siteId, " +                  // (1)
                "s.locationId, " +              // (2)
                "t.locationTypeId, " +          // (3)
                "t.boundAdminLevelId, " +       // (4)
                "k.adminLevelId, " +            // (5)
                "k.adminEntityId " +            // (6)
                "FROM site s " +
                "LEFT JOIN location g ON (s.locationId = g.locationId) " +
                "LEFT JOIN locationtype t ON (g.locationTypeId = t.locationTypeId) " +
                "LEFT JOIN locationadminlink k ON (s.locationId = k.locationId) " +
                "WHERE s.deleted = 0 AND s.activityId = " + activity.getId();

        if(siteId != null) {
            sql += " AND s.siteId=" + siteId;
        }

        sql +=  " ORDER BY s.siteId";

        System.out.println(sql);

        int lastSiteId = -1;
        boolean foundEntry = false;
        try(ResultSet rs = executor.query(sql)) {
            while(rs.next()) {
                int siteId = rs.getInt(1);

                if(lastSiteId > 0 && lastSiteId != siteId) {
                    if(!foundEntry) {
                        emit(null);
                    }
                    foundEntry = false;
                }

                int locationId = rs.getInt(2);
                int locationTypeId = rs.getInt(3);
                int boundAdminLevelId = rs.getInt(4);
                if(rs.wasNull()) {
                    // This location is not bound to an admin entity, just return this id
                    // if we haven't already
                    if(siteId != lastSiteId) {
                        foundEntry = true;
                        emit(new ReferenceValue(
                                new RecordRef(
                                        CuidAdapter.locationFormClass(locationTypeId),
                                        CuidAdapter.locationInstanceId(locationId))));
                    }
                } else {
                    int adminLevelId = rs.getInt(5);
                    if(boundAdminLevelId == adminLevelId) {
                        int adminEntityId = rs.getInt(6);
                        foundEntry = true;
                        emit(new ReferenceValue(
                                new RecordRef(
                                        CuidAdapter.adminLevelFormClass(adminLevelId),
                                        CuidAdapter.entity(adminEntityId))));
                    }
                }
                lastSiteId = siteId;
            }
        }

        if(lastSiteId != -1 && !foundEntry) {
            emit(null);
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
