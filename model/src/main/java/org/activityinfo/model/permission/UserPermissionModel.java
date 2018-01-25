package org.activityinfo.model.permission;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a user's permissions within a database
 */
public class UserPermissionModel implements JsonSerializable {
    private int userId;
    private int databaseId;
    private List<GrantModel> grants = new ArrayList<>();

    private UserPermissionModel() {
    }

    public UserPermissionModel(int userId, int databaseId, List<GrantModel> grants) {
        this.userId = userId;
        this.databaseId = databaseId;
        this.grants = grants;
    }

    public List<GrantModel> getGrants() {
        return grants;
    }

    @Override
    public JsonValue toJson() {
        JsonValue grantsArray = Json.createArray();
        for (GrantModel grant : grants) {
            grantsArray.add(grant.toJson());
        }

        JsonValue object = Json.createObject();
        object.put("userId", userId);
        object.put("databaseId", databaseId);
        object.put("grants", grantsArray);

        return object;
    }

    public static UserPermissionModel fromJson(JsonValue jsonValue) {
        UserPermissionModel model = new UserPermissionModel();
        model.userId = (int) jsonValue.getNumber("userId");
        model.databaseId = (int)jsonValue.getNumber("databaseId");

        for (JsonValue value : jsonValue.get("grants").values()) {
            model.grants.add(GrantModel.fromJson(value));
        }

        return model;
    }
}
