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
package org.activityinfo.server.authentication;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.logging.Logger;

/**
 * Details of a Humanitarian.id account
 */
public class HumanitarianIdAccount {

    private static final Logger LOGGER = Logger.getLogger(HumanitarianId.class.getName());

    private String id;
    private String email;
    private String name;
    private boolean active;
    
    public static HumanitarianIdAccount parse(byte[] response) {
        String json = new String(response, Charsets.UTF_8);

        LOGGER.info("Account = " + json);

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject)jsonParser.parse(json);
        HumanitarianIdAccount account = new HumanitarianIdAccount();
        account.id = jsonObject.get("user_id").getAsString();
        account.email = jsonObject.get("email").getAsString();
        if(jsonObject.has("name")) {
            account.name = jsonObject.get("name").getAsString();
        }
        return account;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public String getName() {
        return name;
    }
}
