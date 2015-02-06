package org.activityinfo.server.endpoint.jsonrpc;
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

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.GetTargets;
import org.junit.Test;

/**
 * @author yuriyz on 6/24/14.
 */
public class JsonRpcClientTest {

    public static final int ACTIVITY_ID = 1077;

    @Test
    public void getSites() {
        try {
            Filter filter = new Filter();
            filter.addRestriction(DimensionType.Activity, ACTIVITY_ID);

            GetSites getSites = new GetSites();
            getSites.setFilter(filter);

            Object response = testClient().execute(getSites);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonRpcClient testClient() {
        //            String endpoint = "https://ai-dev.appspot.com/command";
//            String username = "test@test.org";
//            String password = "testing123";

        String endpoint = "http://127.0.0.1:8886/command";
        String username = "lisa@solidarites";
        String password = "xyz";

        return JsonRpcClientBuilder.builder().
                endpoint(endpoint).username(username).password(password).build();
    }

    @Test
    public void getTargets() {
        try {

            Filter filter = new Filter();
            filter.addRestriction(DimensionType.Activity, ACTIVITY_ID);

            GetTargets getTargets = new GetTargets();
            getTargets.setDatabaseId(1);
            String response = testClient().execute(getTargets);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
