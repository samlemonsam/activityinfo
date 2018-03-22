package org.activityinfo.model.database;

import org.activityinfo.json.AutoJson;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.annotation.AutoBuilder;
import org.activityinfo.model.resource.ResourceId;

@AutoJson
@AutoBuilder
public class Resource implements JsonSerializable {
    ResourceId id;
    ResourceId parentId;
    ResourceType type;
    String label;

    Resource() {}

    public Resource(ResourceId id, ResourceId parentId, String label) {
        this.id = id;
        this.parentId = parentId;
        this.label = label;
    }

    public ResourceId getId() {
        return id;
    }

    public ResourceId getParentId() {
        return parentId;
    }

    public String getLabel() {
        return label;
    }

    public ResourceType getType() {
        return type;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", id.asString());
        object.put("parentId", parentId.asString());
        object.put("label", label);
        object.put("type", type.name());
        return object;
    }

    public static Resource fromJson(JsonValue object) {
        Resource resource = new Resource();
        resource.id = ResourceId.valueOf(object.getString("id"));
        resource.parentId = ResourceId.valueOf(object.getString("parentId"));
        resource.label = object.getString("label");
        resource.type = ResourceType.valueOf(object.getString("type"));
        return resource;
    }

}
