package org.activityinfo.model.permission;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.resource.ResourceId;

/**
 * <p>Request permission to perform an {@link Operation} on a given {@link Resource}, within the specified
 * {@code database} and by the specified {@code user}.</p>
 *
 * <p>The corresponding {@link Permission} returned by the system permits or denies the user from performing the
 * requested operation.</p>
 */
public class PermissionQuery implements JsonSerializable {

    private int user;
    private int database;
    private Operation operation;
    private ResourceId resourceId;

    public PermissionQuery() {
    }

    public PermissionQuery(int user, int database, Operation operation, ResourceId resourceId) {
        this.user = user;
        this.database = database;
        this.operation = operation;
        this.resourceId = resourceId;
    }

    public int getUser() {
        return user;
    }

    public int getDatabase() {
        return database;
    }

    public Operation getOperation() {
        return operation;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    public static PermissionQuery fromJson(JsonValue object) {
        PermissionQuery query = new PermissionQuery();
        query.user = object.get("user").asInt();
        query.database = object.get("database").asInt();
        query.operation = Operation.valueOf(object.get("operation").asString());
        query.resourceId = ResourceId.valueOf(object.get("resource").asString());
        return query;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("user", user);
        object.put("database", database);
        object.put("operation", operation.name());
        object.put("resource", resourceId.asString());
        return object;
    }

}
