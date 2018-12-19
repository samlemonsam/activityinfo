package org.activityinfo.model.permission;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import javax.annotation.Nullable;
import java.util.Optional;

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
        this.filter = Optional.empty();
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

    public boolean isForbidden() {
        return !isPermitted();
    }

    public void setPermitted(boolean permitted) {
        this.permitted = permitted;
    }

    public Optional<String> getOptionalFilter() {
        return filter;
    }

    public @Nullable String getFilter() {
        return filter.orElse(null);
    }

    public void setFilter(Optional<String> filter) {
        this.filter = filter;
    }

    public boolean isFiltered() {
        return filter.isPresent() && !filter.get().isEmpty();
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
        permission.filter = Optional.ofNullable(object.get("filter").asString());
        return permission;
    }
}
