/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.model.database;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Describes a single user's view of database, including the folders, forms,
 * and locks visible to the user, as well as their own permissions within this database.
 */
public class UserDatabaseMeta implements JsonSerializable {

    public static final String VERSION_SEP = "#";

    private ResourceId databaseId;
    private int userId;
    private String label;
    private boolean visible;
    private boolean owner;
    private boolean published = false;
    private boolean pendingTransfer;
    private String version;
    private boolean suspended;

    private final Map<ResourceId, Resource.Node> resourceNodeMap = new HashMap<>();
    private final Map<ResourceId, Resource> resources = new HashMap<>();
    private final Map<ResourceId, GrantModel> grants = new HashMap<>();
    private final Multimap<ResourceId, RecordLock> locks = HashMultimap.create();

    public ResourceId getDatabaseId() {
        return databaseId;
    }

    public int getLegacyDatabaseId() {
        return CuidAdapter.getLegacyIdFromCuid(databaseId);
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

    public boolean isPublished() {
        return published;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public Collection<Resource> getResources() {
        return resources.values();
    }

    public boolean hasResource(ResourceId resourceId) {
        return resources.containsKey(resourceId);
    }

    public Optional<Resource> getResource(ResourceId resourceId) {
        return Optional.ofNullable(resources.get(resourceId));
    }

    public List<Resource> getRootResources() {
        return resources.values().stream()
                .filter(r -> r.getParentId().equals(databaseId))
                .collect(Collectors.toList());
    }

    public boolean hasGrant(ResourceId resourceId) {
        return grants.containsKey(resourceId);
    }

    public Optional<GrantModel> getGrant(ResourceId resourceId) {
        return Optional.ofNullable(grants.get(resourceId));
    }

    public Collection<RecordLock> getLocks() {
        return locks.values();
    }

    public RecordLockSet getEffectiveLocks(ResourceId resourceId) {
        List<RecordLock> effective = new ArrayList<>();
        do {
            effective.addAll(this.locks.get(resourceId));
            Resource resource = resources.get(resourceId);
            if(resource == null) {
                break;
            }
            resourceId = resource.getParentId();
        } while(true);

        return new RecordLockSet(effective);
    }

    public String getVersion() {
        return version;
    }

    public long getDatabaseVersion() {
        return isOwner()
                ? Long.valueOf(version)
                : Long.valueOf(version.substring(0,version.indexOf(VERSION_SEP)));
    }

    public long getUserVersion() {
        return isOwner()
                ? Long.valueOf(version)
                : Long.valueOf(version.substring(version.indexOf(VERSION_SEP)+1, version.length()));
    }

    public boolean isPendingTransfer() {
        assert isOwner() : "User is not the owner of the database.";
        return pendingTransfer;
    }

    public List<CatalogEntry> findCatalogEntries(ResourceId parentId) {
        if (!resourceNodeMap.containsKey(parentId) && !parentId.equals(databaseId)) {
            return Collections.emptyList();
        }
        Resource.Node parentNode = getParentNode(parentId);
        if (parentNode.isLeaf()) {
            return Collections.emptyList();
        }
        return collectCatalogEntries(parentNode.getChildNodes(), Lists.newArrayList());
    }

    private Resource.Node getParentNode(ResourceId parentId) {
        if (parentId.equals(databaseId)) {
            Resource.Node databaseNode = new Resource.Node(null, null);
            databaseNode.addChildNodes(getRootResourceNodes());
            return databaseNode;
        }
        return resourceNodeMap.get(parentId);
    }

    private List<Resource.Node> getRootResourceNodes() {
        return getRootResources().stream()
                .map(r -> resourceNodeMap.get(r.getId()))
                .collect(Collectors.toList());
    }

    private List<CatalogEntry> collectCatalogEntries(List<Resource.Node> nodes, ArrayList<CatalogEntry> entries) {
        for (Resource.Node node : nodes) {
            Resource resource = node.getResource();
            CatalogEntry entry = buildCatalogEntry(resource);
            entries.add(entry);
            if (!node.isLeaf()) {
                collectCatalogEntries(node.getChildNodes(), entries);
            }
        }
        return entries;
    }

    private CatalogEntry buildCatalogEntry (Resource resource) {
        return new CatalogEntry(resource.getId().asString(), resource.getLabel(), catalogType(resource.getType()));
    }

    private CatalogEntryType catalogType(ResourceType type) {
        switch (type) {
            case DATABASE:
            case FOLDER:
                return CatalogEntryType.FOLDER;
            case FORM:
            default:
                return CatalogEntryType.FORM;
        }
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", databaseId.asString());
        object.put("version", version);
        object.put("label", label);
        object.put("visible", visible);
        object.put("owner", owner);
        if (owner) {
            object.put("pendingTransfer", pendingTransfer);
        }
        if (published) {
            object.put("published", published);
        }
        object.put("userId", userId);
        object.put("resources", Json.toJsonArray(resources.values()));
        object.put("locks", Json.toJsonArray(locks.values()));
        object.put("grants", Json.toJsonArray(grants.values()));
        object.put("suspended", suspended);
        return object;
    }

    public static UserDatabaseMeta fromJson(JsonValue object) {
        UserDatabaseMeta meta = new UserDatabaseMeta();
        meta.databaseId = ResourceId.valueOf(object.getString("id"));
        meta.userId = (int) object.getNumber("userId");
        meta.version = object.getString("version");
        meta.label = object.getString("label");
        meta.visible = object.getBoolean("visible");
        meta.owner = object.getBoolean("owner");
        meta.suspended = object.getBoolean("suspended");
        if (meta.owner) {
            meta.pendingTransfer = object.getBoolean("pendingTransfer");
        }
        if (object.hasKey("published")) {
            meta.published = object.getBoolean("published");
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

        JsonValue grantsArray = object.get("grants");
        for (int i = 0; i < grantsArray.length(); i++) {
            GrantModel grant = GrantModel.fromJson(grantsArray.get(i));
            assert !meta.grants.containsKey(grant.getResourceId()) : "Cannot define more than 1 Grant for a given Resource.";
            meta.grants.put(grant.getResourceId(), grant);
        }
        return meta;
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

        public Builder setDatabaseId(ResourceId id) {
            meta.databaseId = id;
            return this;
        }

        public Builder setDatabaseId(int id) {
            return setDatabaseId(CuidAdapter.databaseId(id));
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
            return this;
        }

        public Builder setPublished(boolean published) {
            meta.published = published;
            return this;
        }

        public Builder setSuspended(boolean suspended) {
            meta.suspended = suspended;
            return this;
        }

        public Builder setPendingTransfer(boolean pendingTransfer) {
            meta.pendingTransfer = pendingTransfer;
            return this;
        }

        public Builder addGrants(Collection<GrantModel> grants) {
            for (GrantModel grant : grants) {
                assert !meta.grants.containsKey(grant.getResourceId()) : "Cannot define more than 1 Grant for a given Resource.";
                meta.grants.put(grant.getResourceId(), grant);
            }
            return this;
        }

        public Builder addLock(RecordLock lock) {
            meta.locks.put(lock.getResourceId(), lock);
            return this;
        }

        public Builder addLocks(Collection<RecordLock> locks) {
            for (RecordLock lock : locks) {
                addLock(lock);
            }
            return this;
        }

        public Builder addResources(Collection<Resource> resources) {
            for (Resource resource : resources) {
                addResource(resource);
            }
            return this;
        }

        public Builder addResource(Resource resource) {
            meta.resources.put(resource.getId(), resource);
            return this;
        }

        private boolean isVisible() {
            return meta.owner
                    || meta.published
                    || !meta.grants.isEmpty()
                    || hasPublicResources();
        }

        private boolean hasPublicResources() {
            for (Resource resource : meta.resources.values()) {
                if (resource.isPublic()) {
                    return true;
                }
            }
            return false;
        }

        private void removePrivateResources() {
            meta.resources.values().removeIf(resource -> !resource.isPublic());
        }

        private void constructResourceNodeMap() {
            if (meta.resources.isEmpty()) {
                return;
            }
            meta.resources.values().forEach(this::constructNode);
        }

        private void constructNode(Resource resource) {
            Resource.Node node = new Resource.Node(meta.resourceNodeMap.get(resource.getParentId()), resource);
            if (!node.isRoot()) {
                node.getParentNode().addChildNode(node);
            }
            meta.resourceNodeMap.put(resource.getId(), node);
        }

        public UserDatabaseMeta build() {
            meta.visible = isVisible();
            // If not visible to current user, strip all resources, grants and lock information before building
            if (!meta.visible) {
                meta.resources.clear();
                meta.grants.clear();
                meta.locks.clear();
            }
            // If database is visible to current user, but the user is not the owner and has no explicit grants,
            // then remove any private resources
            if (meta.visible && !meta.owner && meta.grants.isEmpty()) {
                removePrivateResources();
            }
            constructResourceNodeMap();
            return meta;
        }
    }
}
