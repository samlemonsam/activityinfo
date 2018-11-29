package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseGrant implements JsonSerializable {

    private int userId;
    private ResourceId databaseId;
    private long version;

    private final Map<ResourceId, GrantModel> grants = new HashMap<>();

    public int getUserId() {
        return userId;
    }

    public ResourceId getDatabaseId() {
        return databaseId;
    }

    public long getVersion() {
        return version;
    }

    public Map<ResourceId, GrantModel> getGrants() {
        return grants;
    }

    public DatabaseGrant fromJson(JsonValue object) {
        DatabaseGrant databaseGrant = new DatabaseGrant();
        databaseGrant.userId = object.get("userId").asInt();
        databaseGrant.databaseId = ResourceId.valueOf(object.get("databaseId").asString());
        databaseGrant.version = object.get("version").asLong();
        JsonValue grantsArray = object.get("grants");
        for (int i = 0; i < grantsArray.length(); i++) {
            GrantModel grant = GrantModel.fromJson(grantsArray.get(i));
            databaseGrant.grants.put(grant.getResourceId(), grant);
        }
        return databaseGrant;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("userId", userId);
        object.put("databaseId", databaseId.asString());
        object.put("version", version);
        object.put("grants", Json.toJsonArray(grants.values()));
        return object;
    }

    public static class Builder {

        private DatabaseGrant dbGrant = new DatabaseGrant();

        public Builder setUserId(int userId) {
            dbGrant.userId = userId;
            return this;
        }

        public Builder setDatabaseId(ResourceId databaseId) {
            dbGrant.databaseId = databaseId;
            return this;
        }

        public Builder setVersion(long version) {
            dbGrant.version = version;
            return this;
        }

        public Builder addGrants(List<GrantModel> grants) {
            for (GrantModel grant : grants) {
                addGrant(grant);
            }
            return this;
        }

        public Builder addGrant(GrantModel grant) {
            dbGrant.grants.put(grant.getResourceId(), grant);
            return this;
        }

        public DatabaseGrant build() {
            return dbGrant;
        }

    }


}
