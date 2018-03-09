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
package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.google.common.base.Function;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;

import java.util.*;

class LocationTypeProxy implements DataProxy<ListLoadResult<LocationTypeEntry>> {

    private Dispatcher dispatcher;
    private int countryId;

    LocationTypeProxy(Dispatcher dispatcher, int countryId) {
        this.dispatcher = dispatcher;
        this.countryId = countryId;
    }

    @Override
    public void load(DataReader<ListLoadResult<LocationTypeEntry>> reader, Object loadConfig, AsyncCallback<ListLoadResult<LocationTypeEntry>> callback) {
        dispatcher.execute(new GetSchema())
        .then(new Function<SchemaDTO, ListLoadResult<LocationTypeEntry>>() {
            @Override
            public ListLoadResult<LocationTypeEntry> apply(SchemaDTO schema) {

                // Build a dictionary of databases that have been shared with the user
                Map<Integer, String> databaseNames = new HashMap<>();
                for (UserDatabaseDTO db : schema.getDatabases()) {
                    databaseNames.put(db.getId(), db.getName());
                }

                List<LocationTypeEntry> list = new ArrayList<>();

                for (LocationTypeDTO locationType : schema.getCountryById(countryId).getLocationTypes()) {
                    if(!locationType.isDeleted()) {
                        if (locationType.getDatabaseId() == null) {
                            list.add(new LocationTypeEntry(locationType));
                        } else {
                            list.add(new LocationTypeEntry(locationType, databaseNames.get(locationType.getDatabaseId())));
                        }
                    }
                }

                Collections.sort(list);

                return new BaseListLoadResult<>(list);
            }
        })
        .then(callback);
    }

}
