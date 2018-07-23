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
package org.activityinfo.test.api;

import org.activityinfo.client.ActivityInfoClient;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.ApiApplicationDriver;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.junit.rules.TestRule;

/**
 * Harness for tests written directly against the API.
 */
public class ApiTestHarness {

    private ApiApplicationDriver driver;


    /**
     * The logged in user.
     */
    private UserAccount currentUser;
    private final DevServerAccounts accounts;
    private final Server server;
    private final AliasTable aliasTable;

    public ApiTestHarness() {
        server = new Server();
        aliasTable = new AliasTable();
        accounts = new DevServerAccounts(server);
        this.driver = new ApiApplicationDriver(server, accounts, aliasTable);

        System.out.println("Server: " + server.getRootUrl());
    }

    private UserAccount currentUser() {
        if(currentUser == null) {
            currentUser = accounts.any();
            System.out.println("Logged in as " + currentUser);
        }
        return currentUser;
    }

    public ActivityInfoClient client() {
        UserAccount user = currentUser();
        return new ActivityInfoClient(server.getRootUrl(), user.getEmail(), user.getPassword());
    }

    public TestDatabase createDatabase() {
        String name = aliasTable.createAlias("db");
        ActivityInfoClient client = client();

        ResourceId databaseId = client.createDatabase(name);
        int id = CuidAdapter.getLegacyIdFromCuid(databaseId);

        // Query list of partners
        QueryModel queryModel = new QueryModel(CuidAdapter.partnerFormId(id));
        queryModel.selectResourceId().as("id");

        ColumnSet columnSet = client.queryTable(queryModel);
        ColumnView partnerIdView = columnSet.getColumnView("id");

        ResourceId firstPartnerId = ResourceId.valueOf(partnerIdView.getString(0));

        return new TestDatabase(databaseId, name, firstPartnerId);
    }


    public ResourceId newFormId() {
        return CuidAdapter.generateActivityId();
    }

    public ResourceId newFieldId() {
        return CuidAdapter.generateIndicatorId();
    }

    public ResourceId newRecordId() {
        return CuidAdapter.generateSiteCuid();
    }
}
