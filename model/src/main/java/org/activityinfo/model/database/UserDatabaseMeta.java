package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a single user's view of database, including the folders, forms,
 * and locks visible to the user, as well as their own permissions within this database.
 */
public class UserDatabaseMeta {
    private int databaseId;
    private int userId;
    private String label;
    private boolean visible;
    private boolean owner;
    private String version;

    private final List<Resource> resources = new ArrayList<>();
    private List<GrantModel> grants = new ArrayList<>();
    private List<DatabaseLock> locks = new ArrayList<>();

    public int getDatabaseId() {
        return databaseId;
    }

    public int getUserId() {
        return userId;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isOwner() {
        return owner;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<GrantModel> getGrants() {
        return grants;
    }

    public List<DatabaseLock> getLocks() {
        return locks;
    }

    public String getVersion() {
        return version;
    }

    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", databaseId);
        object.put("version", version);
        object.put("label", label);
        object.put("visible", visible);
        object.put("owner", owner);
        object.put("userId", userId);
        object.put("resources", Json.toJsonArray(resources));
        object.put("locks", Json.toJsonArray(locks));
        object.put("grants", Json.toJsonArray(grants));
        return object;
    }


    public static class Builder {
        private final UserDatabaseMeta meta = new UserDatabaseMeta();

        public Builder() {
            meta.version = "0";
        }

        public Builder setVersion(String version) {
            meta.version = version;
            return this;
        }

        public Builder setDatabaseId(int id) {
            meta.databaseId = id;
            return this;
        }

        public Builder setUserId(int userId) {
            meta.userId = userId;
            return this;
        }

        public Builder setLabel(String label) {
            meta.label = label;
            return this;
        }

        public Builder setOwner(boolean owner) {
            meta.owner = owner;
            meta.visible = true;
            return this;
        }


        public Builder addGrants(List<GrantModel> userGrants) {
            meta.grants.addAll(userGrants);
            return this;
        }

        public Builder addLocks(List<DatabaseLock> databaseLocks) {
            meta.locks.addAll(databaseLocks);
            return this;
        }

        public void addResources(List<Resource> forms) {
            meta.resources.addAll(forms);
        }

        public boolean isVisible() {
            return meta.owner || !meta.grants.isEmpty();
        }

        public UserDatabaseMeta build() {
            return meta;
        }
    }
}
