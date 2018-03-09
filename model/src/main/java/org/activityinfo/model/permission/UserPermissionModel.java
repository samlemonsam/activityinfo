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
package org.activityinfo.model.permission;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a user's permissions within a database
 */
public class UserPermissionModel implements JsonSerializable {
    private int userId;
    private int databaseId;
    private List<GrantModel> grants = new ArrayList<>();

    private UserPermissionModel() {
    }

    public UserPermissionModel(int userId, int databaseId, List<GrantModel> grants) {
        this.userId = userId;
        this.databaseId = databaseId;
        this.grants = grants;
    }

    public List<GrantModel> getGrants() {
        return grants;
    }

    @Override
    public JsonValue toJson() {
        JsonValue grantsArray = Json.createArray();
        for (GrantModel grant : grants) {
            grantsArray.add(grant.toJson());
        }

        JsonValue object = Json.createObject();
        object.put("userId", userId);
        object.put("databaseId", databaseId);
        object.put("grants", grantsArray);

        return object;
    }

    public static UserPermissionModel fromJson(JsonValue jsonValue) {
        UserPermissionModel model = new UserPermissionModel();
        model.userId = (int) jsonValue.getNumber("userId");
        model.databaseId = (int)jsonValue.getNumber("databaseId");

        for (JsonValue value : jsonValue.get("grants").values()) {
            model.grants.add(GrantModel.fromJson(value));
        }

        return model;
    }
}
