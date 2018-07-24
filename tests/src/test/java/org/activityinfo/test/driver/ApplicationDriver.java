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
package org.activityinfo.test.driver;

import com.codahale.metrics.Meter;
import org.activityinfo.client.ActivityInfoClient;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.test.api.TestDatabase;
import org.activityinfo.test.capacity.Metrics;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;

import java.util.logging.Logger;

public class ApplicationDriver {

    public static final Logger LOGGER = Logger.getLogger(ApplicationDriver.class.getName());

    public static final Meter COMMAND_RATE = Metrics.REGISTRY.meter("COMMAND_RATE");
    public static final ApiErrorRate ERROR_RATE = new ApiErrorRate();
    private final Server server;
    private final AliasTable aliasTable;
    private final UserAccount account;
    private final ActivityInfoClient client;

    public ApplicationDriver(Server server, AliasTable aliasTable, UserAccount account) {
        this.server = server;
        this.aliasTable = aliasTable;
        this.account = account;
        this.client = new ActivityInfoClient(server.getRootUrl(), account.getEmail(), account.getPassword());
    }

    public TestDatabase createDatabase() {
        return createDatabase("db");
    }

    public TestDatabase createDatabase(String databaseName) {
        String name = aliasTable.createAlias(databaseName);

        ResourceId databaseId = client.createDatabase(name);
        int id = CuidAdapter.getLegacyIdFromCuid(databaseId);

        // Query list of partners
        QueryModel queryModel = new QueryModel(CuidAdapter.partnerFormId(id));
        queryModel.selectRecordId().as("id");

        ColumnSet columnSet = client.queryTable(queryModel);
        ColumnView partnerIdView = columnSet.getColumnView("id");

        ResourceId firstPartnerId = ResourceId.valueOf(partnerIdView.getString(0));

        return new TestDatabase(databaseId, name, firstPartnerId);
    }


    public ActivityInfoClient getClient() {
        return client;
    }
}
