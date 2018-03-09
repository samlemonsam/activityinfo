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
package org.activityinfo.test.sut;

import com.google.inject.AbstractModule;
import cucumber.runtime.java.guice.ScenarioScoped;


public class SystemUnderTest extends AbstractModule {

    private Server server;

    public SystemUnderTest(String testUrl) {
        this.server = new Server(testUrl);
    }

    public SystemUnderTest() {
        this.server = new Server();
    }

    @Override
    protected void configure() {
        
        bind(Server.class).toInstance(server);
        bind(Accounts.class).to(DevServerAccounts.class).in(ScenarioScoped.class);
        bind(DevServerAccounts.class).in(ScenarioScoped.class);

    }
}
