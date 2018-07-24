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

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.Server;

/**
 * Harness for tests written directly against the API.
 */
public class ApiTestHarness {

    private final DevServerAccounts accounts;
    private final Server server;
    private final AliasTable aliasTable;

    public ApiTestHarness() {
        server = new Server();
        aliasTable = new AliasTable();
        accounts = new DevServerAccounts(server);

        System.out.println("Server: " + server.getRootUrl());
    }

    public ApplicationDriver newUser() {
        return new ApplicationDriver(server, aliasTable, accounts.any());
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
