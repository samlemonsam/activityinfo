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
package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

/**
 * Grants a user a set of permissions within a folder
 */
public class GrantModel implements JsonSerializable {

    private String id;
    private String label;
    private String resourceId;

    private GrantModel() {
    }

    public GrantModel(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * @return this grant's unique id within the database.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the id of the database, folder, or form to which this grant applies.
     */
    public String getResourceId() {
        return resourceId;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("folderId", resourceId);
        return object;
    }

    public static GrantModel fromJson(JsonValue value) {
        GrantModel grant = new GrantModel();
        grant.resourceId = value.getString("folderId");
        return grant;
    }
}
