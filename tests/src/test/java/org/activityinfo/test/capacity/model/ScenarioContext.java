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
package org.activityinfo.test.capacity.model;

import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.TestContext;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.Server;

import java.util.List;

/**
 * Represents an independent test scenario which might involve multiple simulated users.
 * Each scenario has its own alias table so it can run completely independently of another scenario
 */
public class ScenarioContext {

    private TestContext testContext;
    private DevServerAccounts accounts;
    private AliasTable aliasTable;

    private List<User> users = Lists.newArrayList();

    public ScenarioContext(TestContext context) {
        this.testContext = context;
        this.accounts = new DevServerAccounts(testContext.getServer());
        this.aliasTable = new AliasTable();
    }

    public DevServerAccounts getAccounts() {
        return accounts;
    }

    public Server getServer() {
        return testContext.getServer();
    }

    public AliasTable getAliasTable() {
        return aliasTable;
    }

    public User user(UserRole role) {
        User user = new User(this, role);
        users.add(user);
        return user;
    }
    
}
