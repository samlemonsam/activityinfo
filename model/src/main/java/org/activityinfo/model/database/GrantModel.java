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
import org.activityinfo.model.resource.ResourceId;

import java.util.HashSet;
import java.util.Set;

/**
 * Grants a user permission to perform specified operations within a given resource,
 * with a set of record filters applied
 */
public class GrantModel implements JsonSerializable {

    private String id;
    private ResourceId resourceId;
    private Set<Operation> operations = new HashSet<>();
    private Set<String> filters = new HashSet<>();

    private GrantModel() {
    }

    /**
     * @return This grant's unique id within the database.
     */
    public String getId() {
        return id;
    }

    /**
     * @return The id of the {@link Resource} to which this grant applies. The {@code Resource} may be a
     * {@link ResourceType#FORM}, {@link ResourceType#FOLDER}, or {@link ResourceType#DATABASE}
     */
    public ResourceId getResourceId() {
        return resourceId;
    }

    /**
     * @return The set of {@link Operation}s the user is allowed to perform within the {@link Resource}
     * this grant applies to.
     */
    public Set<Operation> getOperations() {
        return operations;
    }

    /**
     * @return The set of filters to be applied within the {@link Resource} this grant applies to.
     */
    public Set<String> getFilters() {
        return filters;
    }

    @Override
    public JsonValue toJson() {

        JsonValue operationArray = Json.createArray();
        for (Operation operation : operations) {
            operationArray.add(Json.create(operation.name()));
        }

        JsonValue filterArray = Json.createArray();
        for (String filter : filters) {
            filterArray.add(Json.create(filter));
        }

        JsonValue object = Json.createObject();
        object.put("resourceId", resourceId.asString());
        object.put("operations", operationArray);
        object.put("filters", filterArray);
        return object;
    }

    public static GrantModel fromJson(JsonValue object) {
        GrantModel grant = new GrantModel();

        // Must accommodate legacy grant models (which specified only folder grants)
        JsonValue resourceId = object.hasKey("folderId") ? object.get("folderId") : object.get("resourceId");
        grant.resourceId = ResourceId.valueOf(resourceId.asString());

        if (object.hasKey("operations") && object.get("operations").isJsonArray()) {
            JsonValue operationsArray = object.get("operations");
            for (int i=0; i<operationsArray.length(); i++) {
                grant.operations.add(Operation.valueOf(operationsArray.get(i).asString()));
            }
        }

        if (object.hasKey("filters") && object.get("filters").isJsonArray()) {
            JsonValue filterArray = object.get("filters");
            for (int i=0; i<filterArray.length(); i++) {
                grant.filters.add(filterArray.get(i).asString());
            }
        }

        return grant;
    }

    public static class Builder {

        private GrantModel model = new GrantModel();

        /**
         * Sets the {@link Resource} to which this grant applies. This can be the id of a
         * {@link ResourceType#FORM}, {@link ResourceType#FOLDER} or {@link ResourceType#DATABASE}.
         */
        public Builder setResourceId(ResourceId resourceId) {
            model.resourceId = resourceId;
            return this;
        }

        /**
         * Adds a permitted {@link Operation} to this grant.
         */
        public void addOperation(Operation operation) {
            model.operations.add(operation);
        }

        /**
         * Adds a filter to this grant. This filter is applied to the records within the
         * {@link Resource} this grant applies to.
         */
        public void addFilter(String recordFilter) {
            model.filters.add(recordFilter);
        }

        /**
         * Adds a permitted {@link Operation} and filter to this grant.
         */
        public void addOperation(Operation operation, String recordFilter) {
            addOperation(operation);
            addFilter(recordFilter);
        }

        public GrantModel build() {
            return model;
        }
    }
}
