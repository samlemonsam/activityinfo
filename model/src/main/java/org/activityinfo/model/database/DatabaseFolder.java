package org.activityinfo.model.database;


import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

public class DatabaseFolder implements JsonSerializable {
    private String id;
    private String parentId;
    private String label;

    public DatabaseFolder(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public DatabaseFolder(String id, String parentId, String label) {
        this.id = id;
        this.parentId = parentId;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getLabel() {
        return label;
    }


    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", id);
        object.put("parentId", parentId);
        object.put("label", label);
        return object;
    }
}
