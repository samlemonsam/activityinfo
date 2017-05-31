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
import java.util.List;
import java.util.logging.Logger;

public class BoundLocationBuilder {

    private static final Logger LOGGER = Logger.getLogger(BoundLocationBuilder.class.getName());

    private Integer siteId;

    private List<CursorObserver<FieldValue>> observers = Lists.newArrayList();
    private Activity activity;

    public BoundLocationBuilder(Activity activity) {
        this.activity = activity;
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

        if(activity.getAdminLevelId() == null) {
            LOGGER.severe("Activity " + activity.getId() + " is not bound to an admin level");
            throw new IllegalStateException("Activity not bound to admin level");
        }

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
                    emit(new ReferenceValue(
                            new RecordRef(
                                referenceFormId,
                                CuidAdapter.entity(adminEntityId))));

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
