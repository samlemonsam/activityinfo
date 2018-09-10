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

import com.google.common.base.Optional;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Grants a user permission to perform specified operations within a given resource,
 * with a set of record filters applied
 */
public class GrantModel implements JsonSerializable {

    private String id;
    private ResourceId resourceId;
    private Map<Operation,Optional<String>> operations = new HashMap<>();

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
        return operations.keySet();
    }

    public boolean hasOperation(Operation operation) {
        return operations.containsKey(operation);
    }

    /**
     * @return The (optional) filter to be applied when performing the specified operation on the
     * {@link Resource} this grant applies to.
     */
    public Optional<String> getFilter(Operation operation) {
        assert operations.containsKey(operation);
        return operations.get(operation);
    }

    @Override
    public JsonValue toJson() {
        JsonValue opArray = Json.createArray();
        for (Map.Entry<Operation, Optional<String>> op : operations.entrySet()) {
            JsonValue opObject = Json.createObject();
            opObject.add("operation", Json.create(op.getKey().name()));
            opObject.add("filter", op.getValue().isPresent() ? Json.create(op.getValue().get()) : Json.createNull());
            opArray.add(opObject);
        }

        JsonValue object = Json.createObject();
        object.put("resourceId", resourceId.asString());
        object.put("operations", opArray);
        return object;
    }

    public static GrantModel fromJson(JsonValue object) {
        GrantModel grant = new GrantModel();

        // Must accommodate legacy grant models (which specified only folder grants)
        JsonValue resourceId = object.hasKey("folderId") ? object.get("folderId") : object.get("resourceId");
        grant.resourceId = ResourceId.valueOf(resourceId.asString());

        if (object.hasKey("operations") && object.get("operations").isJsonArray()) {
            JsonValue opArray = object.get("operations");
            for (int i=0; i<opArray.length(); i++) {
                JsonValue opObject = opArray.get(i);
                Operation operation = Operation.valueOf(opObject.get("operation").asString());
                Optional<String> filter = Optional.fromNullable(opObject.get("filter").asString());
                grant.operations.put(operation, filter);
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
            model.operations.put(operation, Optional.absent());
        }

        /**
         * Adds a permitted {@link Operation} and filter to this grant.
         */
        public void addOperation(Operation operation, String recordFilter) {
            model.operations.put(operation, Optional.of(recordFilter));
        }

        /**
         * Adds a filter to the specified operation on this grant. This filter is applied to the records
         * within the {@link Resource} this grant applies to when performing the specified operation.
         */
        public void addFilter(Operation operation, String recordFilter) {
            model.operations.put(operation, Optional.of(recordFilter));
        }

        public GrantModel build() {
            return model;
        }
    }
}
