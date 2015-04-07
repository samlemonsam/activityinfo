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
import com.bedatadriven.rebar.sync.server.JpaUpdateBuilder;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSyncRegionUpdates;
import org.activityinfo.legacy.shared.command.result.SyncRegionUpdate;
import org.activityinfo.legacy.shared.impl.Tables;
import org.activityinfo.server.database.hibernate.entity.AdminEntity;
import org.activityinfo.server.database.hibernate.entity.AdminLevel;
import org.activityinfo.server.database.hibernate.entity.Country;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import java.io.IOException;

public class AdminUpdateBuilder implements UpdateBuilder {
    private EntityManager em;
    private int levelId;
    private AdminLocalState localState;
    public static final int LAST_VERSION_NUMBER = 1;
    private SqliteBatchBuilder batch;

    @Inject
    public AdminUpdateBuilder(EntityManager em) {
        this.em = em;
    }

    @Override
    public SyncRegionUpdate build(User user, GetSyncRegionUpdates request) throws IOException {
        parseLevelId(request);
        localState = new AdminLocalState(request.getLocalVersion());

        SyncRegionUpdate update = new SyncRegionUpdate();
        batch = new SqliteBatchBuilder();

        AdminLevel level = em.find(AdminLevel.class, levelId);
        
        JpaBatchBuilder builder = new JpaBatchBuilder(batch, em);

        if (localState.getVersion() < LAST_VERSION_NUMBER) {
            /*
             * This level is out of date, delete all on the client and send all
             * from the server
             */
            builder.insert(Country.class, "CountryId=" + level.getCountry().getId());
            builder.insert(AdminLevel.class, "AdminLevelId=" + levelId);
            builder.delete(AdminEntity.class, "AdminLevelId=" + levelId);
            builder.insert(AdminEntity.class, "AdminLevelId=" + levelId);
            update.setSql(batch.build());
        }
        update.setComplete(true);
        update.setVersion(Integer.toString(LAST_VERSION_NUMBER));

        return update;
    }

    private void parseLevelId(GetSyncRegionUpdates request) {
        levelId = Integer.parseInt(request.getRegionId().substring("admin/".length()));
    }

}
