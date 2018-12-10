package org.activityinfo.model.database;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

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

    // Resources and Locks present on this database
    private final Map<ResourceId, Resource> resources = new HashMap<>();
    private final Multimap<ResourceId, RecordLock> locks = HashMultimap.create();

    public ResourceId getDatabaseId() {
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

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPublished() {
        return published;
    }

    public boolean isPendingTransfer() {
        return pendingTransfer;
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

        if (deleted) {
            object.put("deleted", deleted);
            return object;
        }

        object.put("label", label);
        object.put("description", Strings.nullToEmpty(description));

        if (published) {
            object.put("published", published);
        }
        if (pendingTransfer) {
            object.put("pendingTransfer", pendingTransfer);
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

        if (object.hasKey("deleted") && object.get("deleted").asBoolean()) {
            meta.deleted = object.get("deleted").asBoolean();
            return meta;
        }

        meta.label = object.getString("label");
        meta.description = object.getString("description");

        if (object.hasKey("published")) {
            meta.published = object.get("published").asBoolean();
        }
        if (object.hasKey("pendingTransfer")) {
            meta.pendingTransfer = object.get("pendingTransfer").asBoolean();
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

        public Builder setDatabaseId(ResourceId databaseId) {
            meta.databaseId = databaseId;
            return this;
        }

        public Builder setOwnerId(int ownerId) {
            meta.ownerId = ownerId;
            return this;
        }

        public Builder setVersion(long verison) {
            meta.version = verison;
            return this;
        }

        public Builder setDeleted(boolean deleted) {
            meta.deleted = deleted;
            return this;
        }

        public Builder setLabel(String label) {
            meta.label = label;
            return this;
        }

        public Builder setDescription(String description) {
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

        public Builder addResources(List<Resource> resources) {
            for (Resource resource : resources) {
                addResource(resource);
            }
            return this;
        }

        public Builder addResource(Resource resource) {
            meta.resources.put(resource.getId(), resource);
            return this;
        }

        public Builder addLocks(List<RecordLock> locks) {
            for (RecordLock lock : locks) {
                addLock(lock);
            }
            return this;
        }

        public Builder addLock(RecordLock lock) {
            meta.locks.put(lock.getResourceId(), lock);
            return this;
        }

        public DatabaseMeta build() {
            return meta;
        }

    }

}
