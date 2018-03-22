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

import org.activityinfo.json.AutoJson;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import java.util.HashSet;
import java.util.Set;

/**
 * Grants a user a set of permissions within a folder
 */
@AutoJson
public class GrantModel implements JsonSerializable {

    private String id;
    private ResourceId resourceId;
    private Set<Operation> operations = new HashSet<>();

    private GrantModel() {
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
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public JsonValue toJson() {

        JsonValue operationsArray = Json.createArray();
        for (Operation operation : operations) {
            operationsArray.add(Json.create(operation.name()));
        }

        JsonValue object = Json.createObject();
        object.put("folderId", resourceId.asString());
        object.put("operations", operationsArray);
        return object;
    }

    public static GrantModel fromJson(JsonValue value) {
        GrantModel grant = new GrantModel();
        grant.resourceId = ResourceId.valueOf(value.getString("folderId"));
        return grant;
    }

    public static class Builder {

        private GrantModel model = new GrantModel();

        /**
         * Sets the resource to which this grant applies. This can be id
         * of a folder, form, or the database itself.
         *
         */
        public Builder setResourceId(ResourceId resourceId) {
            model.resourceId = resourceId;
            return this;
        }

        /**
         * Adds a permitted operation to this grant.
         */
        public void addOperation(Operation operation) {
            model.operations.add(operation);
        }

        public void addOperation(Operation operation, String recordFilter) {
            model.operations.add(operation);
        }

        public GrantModel build() {
            return model;
        }
    }
}
