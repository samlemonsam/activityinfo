package org.activityinfo.server.command.handler.sync;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSyncRegionUpdates;
import org.activityinfo.legacy.shared.command.result.SyncRegionUpdate;
import org.activityinfo.legacy.shared.impl.Tables;
import org.activityinfo.server.database.hibernate.entity.LocationType;
import org.activityinfo.server.database.hibernate.entity.User;
import org.json.JSONException;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

public class LocationUpdateBuilder implements UpdateBuilder {

    public static final String REGION_TYPE = "location";

    private static final int DEFAULT_CHUNK_SIZE = 50; // items in chunk

    private final EntityManager em;
    private long localVersion;
    private SqliteBatchBuilder batch;

    private int typeId;
    private int chunkSize;

    @Inject
    public LocationUpdateBuilder(EntityManager em) {
        this(em, DEFAULT_CHUNK_SIZE);
    }

    public LocationUpdateBuilder(EntityManager em, int chunkSize) {
        this.em = em;
        this.chunkSize = chunkSize;
    }

    @Override
    public SyncRegionUpdate build(User user, GetSyncRegionUpdates request) throws Exception {

        typeId = request.getRegionId();
        localVersion = request.getLocalVersionNumber();
        batch = new SqliteBatchBuilder();

        SyncRegionUpdate update = new SyncRegionUpdate();

        LocationType locationType = em.find(LocationType.class, typeId);
        if(localVersion == locationType.getVersion()) {
            update.setComplete(true);
            update.setVersion(locationType.getVersion());
        } else {
            JpaBatchBuilder jpaBuilder = new JpaBatchBuilder(batch, em);
            jpaBuilder.insert(LocationType.class, "LocationTypeId=" + typeId);

            long latestVersion = queryLatestVersion(update);
            if (latestVersion > localVersion) {
                queryChanged();
                linkAdminEntities();
            }

            update.setVersion(Long.toString(latestVersion));
            update.setSql(batch.build());
        }
        return update;
    }

    private void queryChanged() {

        SqlQuery query = SqlQuery.select()
                                 .appendColumn("LocationId")
                                 .appendColumn("Name")
                                 .appendColumn("Axe")
                                 .appendColumn("X")
                                 .appendColumn("Y")
                                 .appendColumn("LocationTypeId")
                                 .appendColumn("workflowStatusId")
                                 .from(Tables.LOCATION)
                                 .where("locationTypeId").equalTo(typeId)
                                 .where("version").greaterThan(localVersion);

        batch.insert().into(Tables.LOCATION).from(query).execute(em);
    }

    private void linkAdminEntities() throws JSONException {

        SqlQuery query = SqlQuery.select()
                                 .appendColumn("K.AdminEntityId")
                                 .appendColumn("K.LocationId")
                                 .from(Tables.LOCATION, "L")
                                 .innerJoin(Tables.LOCATION_ADMIN_LINK, "K")
                                 .on("L.LocationId=K.LocationId")
                                 .where("L.locationTypeId").equalTo(typeId)
                                 .where("L.version").greaterThan(localVersion);

        batch.insert().into(Tables.LOCATION_ADMIN_LINK).from(query).execute(em);
    }

    private long queryLatestVersion(SyncRegionUpdate update) throws JSONException {
        SqlQuery query = SqlQuery.select()
                                 .appendColumn("version", "latest")
                                 .from(Tables.LOCATION)
                                 .where("locationTypeId").equalTo(typeId)
                                 .where("version").greaterThan(localVersion);

        List<Long> longs = SqlQueryUtil.queryLongList(em, query);

        if (longs.isEmpty()) {
            update.setComplete(true);
            return localVersion;
        }

        // our intention is to reduce batch, so we cut versions into chunks
        if (longs.size() > chunkSize) {
            longs = longs.subList(0, chunkSize);
            update.setComplete(false);
        } else {
            update.setComplete(true);
        }
        return Collections.max(longs);
    }
}
