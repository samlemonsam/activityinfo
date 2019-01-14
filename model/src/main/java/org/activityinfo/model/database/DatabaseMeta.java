package org.activityinfo.model.database;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Collection of metadata for a Database. Provided by a {@code DatabaseMetaProvider}.</p>
 *
 * <p>{@code DatabaseMeta} defines the <b>shared metadata of a Database</b> in the system. However a User will never
 * access a {@code DatabaseMeta} directly as visibility of the data is dependent on the the rights of the User. </p>
 *
 * <p>Therefore, a {@code DatabaseMeta} and {@link DatabaseGrant} are provided as input to a {@code DatabaseProvider} to
 * produce a {@link UserDatabaseMeta}. A {@link UserDatabaseMeta} defines the metadata of a Database <b>visible to the
 * requesting User.</b></p>
 */
public class DatabaseMeta implements JsonSerializable {

    // Basic data, always visible
    private ResourceId databaseId;
    private int ownerId;
    private long version;

    // Set if database is deleted
    private boolean deleted = false;

    // Label and Description
    private String label;
    private String description;

    // Flags for:
    // - "published" system databases
    // - "pendingTransfer" databases
    private boolean published = false;
    private boolean pendingTransfer = false;
    private boolean suspended = false;

    // Resources and Locks present on this database
    private final Map<ResourceId, Resource> resources = new HashMap<>();
    private final Multimap<ResourceId, RecordLock> locks = HashMultimap.create();

    public @NotNull ResourceId getDatabaseId() {
        return databaseId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public long getVersion() {
        return version;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public @NotNull String getLabel() {
        return label;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public boolean isPublished() {
        return published;
    }

    public boolean isPendingTransfer() {
        return pendingTransfer;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public Map<ResourceId, Resource> getResources() {
        return resources;
    }

    public Multimap<ResourceId, RecordLock> getLocks() {
        return locks;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();

        object.put("id", databaseId.asString());
        object.put("ownerId", ownerId);
        object.put("version", Long.toString(version));

        object.put("label", label);
        if (description != null) {
            object.put("description", description);
        }

        if (deleted) {
            object.put("deleted", deleted);
            return object;
        }

        if (published) {
            object.put("published", published);
        }
        if (pendingTransfer) {
            object.put("pendingTransfer", pendingTransfer);
        }
        if (suspended) {
            object.put("suspended", suspended);
        }

        object.put("resources", Json.toJsonArray(resources.values()));
        object.put("locks", Json.toJsonArray(locks.values()));

        return object;
    }

    public static DatabaseMeta fromJson(JsonValue object) {
        DatabaseMeta meta = new DatabaseMeta();

        meta.databaseId = ResourceId.valueOf(object.getString("id"));
        meta.ownerId = object.get("ownerId").asInt();
        meta.version = Long.valueOf(object.get("version").asString());

        meta.label = object.getString("label");
        if (object.hasKey("description")) {
            meta.description = object.getString("description");
        }

        if (object.hasKey("deleted") && object.get("deleted").asBoolean()) {
            meta.deleted = object.get("deleted").asBoolean();
            return meta;
        }

        if (object.hasKey("published")) {
            meta.published = object.get("published").asBoolean();
        }
        if (object.hasKey("pendingTransfer")) {
            meta.pendingTransfer = object.get("pendingTransfer").asBoolean();
        }
        if (object.hasKey("suspended")) {
            meta.suspended = object.get("suspended").asBoolean();
        }

        JsonValue resourceArray = object.get("resources");
        for (int i = 0; i < resourceArray.length(); i++) {
            Resource resource = Resource.fromJson(resourceArray.get(i));
            meta.resources.put(resource.getId(), resource);
        }
        JsonValue lockArray = object.get("locks");
        for (int i = 0; i < lockArray.length(); i++) {
            RecordLock lock = RecordLock.fromJson(lockArray.get(i));
            meta.locks.put(lock.getResourceId(), lock);
        }

        return meta;
    }

    public static class Builder {

        private final DatabaseMeta meta = new DatabaseMeta();

        public Builder setDatabaseId(@NotNull ResourceId databaseId) {
            meta.databaseId = databaseId;
            return this;
        }

        public Builder setOwnerId(int ownerId) {
            meta.ownerId = ownerId;
            return this;
        }

        public Builder setVersion(long version) {
            meta.version = version;
            return this;
        }

        public Builder setDeleted(boolean deleted) {
            meta.deleted = deleted;
            return this;
        }

        public Builder setLabel(@NotNull String label) {
            meta.label = label;
            return this;
        }

        public Builder setDescription(@Nullable String description) {
            meta.description = description;
            return this;
        }

        public Builder setPublished(boolean published) {
            meta.published = published;
            return this;
        }

        public Builder setPendingTransfer(boolean pendingTransfer) {
            meta.pendingTransfer = pendingTransfer;
            return this;
        }

        public Builder setSuspended(boolean suspended) {
            meta.suspended = suspended;
            return this;
        }

        public Builder addResources(List<Resource> resources) {
            for (Resource resource : resources) {
                addResource(resource);
            }
            return this;
        }

        public Builder addResource(@NotNull Resource resource) {
            meta.resources.put(resource.getId(), resource);
            return this;
        }

        public Builder addLocks(List<RecordLock> locks) {
            for (RecordLock lock : locks) {
                addLock(lock);
            }
            return this;
        }

        public Builder addLock(@NotNull RecordLock lock) {
            meta.locks.put(lock.getResourceId(), lock);
            return this;
        }

        public DatabaseMeta build() {
            return meta;
        }

    }

}
