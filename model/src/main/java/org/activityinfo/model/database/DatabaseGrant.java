package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.resource.ResourceId;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Collection of metadata for a User's permissions on a Database. Provided by a {@code DatabaseGrantProvider}.</p>
 *
 * <p>{@code DatabaseGrant} defines the <b>assigned permissions of a User on a Database</b> in the system. It defines
 * the Resources visible to the User and the permitted operations on those Resources.</p>
 *
 * <p>A {@link DatabaseMeta} and {@code DatabaseGrant} are provided as input to a {@code DatabaseProvider} to
 * produce a {@link UserDatabaseMeta}. A {@link UserDatabaseMeta} defines the metadata of a Database <b>visible to the
 * requesting User.</b></p>
 */
public class DatabaseGrant implements JsonSerializable {

    // Basic data, always visible
    private ResourceId databaseId;
    private int userId;
    private long version;

    // The set of grants assigned to this User.
    private final Map<ResourceId, GrantModel> grants = new HashMap<>();

    public @NotNull ResourceId getDatabaseId() {
        return databaseId;
    }

    public int getUserId() {
        return userId;
    }

    public long getVersion() {
        return version;
    }

    public Map<ResourceId, GrantModel> getGrants() {
        return grants;
    }

    public static DatabaseGrant fromJson(JsonValue object) {
        DatabaseGrant databaseGrant = new DatabaseGrant();

        databaseGrant.userId = object.get("userId").asInt();
        databaseGrant.databaseId = ResourceId.valueOf(object.get("databaseId").asString());
        databaseGrant.version = Long.valueOf(object.get("version").asString());

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
        object.put("version", Long.toString(version));

        object.put("grants", Json.toJsonArray(grants.values()));

        return object;
    }

    public static class Builder {

        private DatabaseGrant dbGrant = new DatabaseGrant();

        public Builder setUserId(int userId) {
            dbGrant.userId = userId;
            return this;
        }

        public Builder setDatabaseId(@NotNull ResourceId databaseId) {
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

        public Builder addGrant(@NotNull GrantModel grant) {
            dbGrant.grants.put(grant.getResourceId(), grant);
            return this;
        }

        public DatabaseGrant build() {
            return dbGrant;
        }

    }


}
