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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;

import javax.inject.Inject;
import java.util.List;

/**
 * Drives the Application through the XForm API 
 */
public class XFormApplicationDriver extends ApplicationDriver {
    private ApiApplicationDriver apiDriver;
    private Server server;
    private UserAccount currentUser;

    @Inject
    public XFormApplicationDriver(ApiApplicationDriver apiDriver, Server server) {
        super(apiDriver.getAliasTable());
        this.apiDriver = apiDriver;
        this.server = server;
    }

    @Override
    public void login() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(UserAccount account) {
        setup().login(account);
        currentUser = account;

    }

    @Override
    public ApplicationDriver setup() {
        return apiDriver;
    }

    @Override
    public ResourceId submitForm(String formName, List<FieldValue> values) throws Exception {
        Client client = Client.create();
        if(currentUser != null) {
            client.addFilter(new HTTPBasicAuthFilter(currentUser.getEmail(), currentUser.getPassword()));

        }
        WebResource root = client.resource(server.getRootUrl());
       // root.path("formList").get()
        throw new UnsupportedOperationException();
    }
}
