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
package org.activityinfo.server.command.handler.sync;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSyncRegionUpdates;
import org.activityinfo.legacy.shared.command.result.SyncRegionUpdate;
import org.activityinfo.server.database.hibernate.entity.AdminLevel;
import org.activityinfo.server.database.hibernate.entity.Country;
import org.activityinfo.server.database.hibernate.entity.LocationType;
import org.activityinfo.server.database.hibernate.entity.User;
import org.json.JSONException;

import javax.persistence.EntityManager;
import java.io.IOException;

public class CountryUpdateBuilder implements UpdateBuilder {

    public static final String REGION_TYPE = "country";

    private final EntityManager entityManager;

    @Inject
    public CountryUpdateBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public SyncRegionUpdate build(User user, GetSyncRegionUpdates request) throws JSONException, IOException {

        int countryId = request.getRegionId();
        
        JpaBatchBuilder batch = new JpaBatchBuilder(new SqliteBatchBuilder(), entityManager);
        
        String inCountry = "countryId = " + countryId;
        
        batch.insert(Country.class, inCountry);
        batch.insert(AdminLevel.class, inCountry);
        batch.delete(LocationType.class, inCountry);
        batch.insert(LocationType.class, inCountry);

        SyncRegionUpdate update = new SyncRegionUpdate();
        update.setComplete(true);
        update.setSql(batch.build());
        update.setVersion("1");
        return update;
    }

}
