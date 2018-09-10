package org.activityinfo.model.database;

import com.google.common.base.Optional;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

/**
 * <p>Response from a {@link PermissionQuery}. Specifies whether the operation requested is permitted.</p>
 * <p>If the operation <i>is</i> permitted, the (optional) filter is to be applied to any queries for this operation.</p>
 */
public class Permission implements JsonSerializable {

    private Operation operation;
    private boolean permitted;
    private Optional<String> filter;

    private Permission() {
    }

    public Permission(Operation operation) {
        this.operation = operation;
    }

    public Permission(Operation operation, boolean permitted, Optional<String> filter) {
        this.operation = operation;
        this.permitted = permitted;
        this.filter = filter;
    }

    public Operation getOperation() {
        return operation;
    }

    public boolean isPermitted() {
        return permitted;
    }

    public void setPermitted(boolean permitted) {
        this.permitted = permitted;
    }

    public Optional<String> getFilter() {
        return filter;
    }

    public void setFilter(Optional<String> filter) {
        this.filter = filter;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("operation", operation.name());
        object.put("permitted", permitted);
        object.add("filter", filter.isPresent() ? Json.create(filter.get()) : Json.createNull());
        return object;
    }

    public static Permission fromJson(JsonValue object) {
        Permission permission = new Permission();
        permission.operation = Operation.valueOf(object.get("operation").asString());
        permission.permitted = object.get("permitted").asBoolean();
        permission.filter = Optional.fromNullable(object.get("filter").asString());
        return permission;
    }
}
