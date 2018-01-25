package org.activityinfo.model.permission;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

/**
 * Grants a user a set of permissions within a folder
 */
public class GrantModel implements JsonSerializable {

    private String folderId;

    public String getFolderId() {
        return folderId;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("folderId", folderId);
        return object;
    }

    public static GrantModel fromJson(JsonValue value) {
        GrantModel grant = new GrantModel();
        grant.folderId = value.getString("folderId");
        return grant;
    }
}
