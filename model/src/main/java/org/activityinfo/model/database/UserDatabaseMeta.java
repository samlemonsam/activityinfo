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

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Collection of metadata for a specific User's view of a Database. Provided by a {@code DatabaseProvider}.</p>
 *
 * <p>{@code UserDatabaseMeta} defines the <b>visible metadata and permissions of a User in a Database</b> in the system.
 * It defines the metadata of the Database visible to the User, the Resources visible to the User,
 * the permitted operations on those Resources, and any Locks on the Database.</p>
 *
 * <p>A {@link DatabaseMeta} and {@link DatabaseGrant} are provided as input to a {@code DatabaseProvider} to produce
 * a {@link UserDatabaseMeta}.</p>
 */
public class UserDatabaseMeta implements JsonSerializable {

    public static final String VERSION_SEP = "#";

    // Basic data, always visible
    private ResourceId databaseId;
    private int userId;
    private String version;

    // Set if database is deleted, or if database is visible to user
    private boolean deleted = false;
    private boolean visible = false;

    // Label and Description
    private String label;
    private String description;

    // Flags for:
    // - "owner" of database
    // - "published" system databases
    // - "pendingTransfer" databases pending transfer to another user
    // - "suspended" database due to billing status
    private boolean owner = false;
    private boolean published = false;
    private boolean pendingTransfer = false;
    private boolean suspended = false;

    // Set of visible Resources and Grants assigned to this User, and Locks on the database
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

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOwner() {
        return owner;
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

    public Collection<GrantModel> getGrants() {
        return grants.values();
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

    public List<CatalogEntry> findCatalogEntries(ResourceId parentId) {
        if (!resourceNodeMap.containsKey(parentId) && !parentId.equals(databaseId)) {
            return Collections.emptyList();
        }
        Resource.Node parentNode = getParentNode(parentId);
        if (parentNode.isLeaf()) {
            return Collections.emptyList();
        }
        // Return the catalog entries for the this nodes _direct_ children
        return parentNode.getChildNodes().stream()
                .map(UserDatabaseMeta::buildCatalogEntry)
                .collect(Collectors.toList());
    }

    private Resource.Node getParentNode(ResourceId parentId) {
        if (parentId.equals(databaseId)) {
            Resource.Node databaseNode = new Resource.Node();
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

    private static CatalogEntry buildCatalogEntry(Resource.Node node) {
        CatalogEntry entry = new CatalogEntry(node.getResource().getId().asString(),
                node.getResource().getLabel(),
                catalogType(node.getResource().getType()));
        entry.setLeaf(node.isLeaf());
        return entry;
    }

    private static CatalogEntryType catalogType(ResourceType type) {
        switch (type) {
            case DATABASE:
            case FOLDER:
                return CatalogEntryType.FOLDER;
            case FORM:
            case SUB_FORM:
            default:
                return CatalogEntryType.FORM;
        }
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();

        object.put("databaseId", databaseId.asString());
        object.put("userId", userId);
        object.put("version", version);

        if (deleted) {
            object.put("deleted", deleted);
            return object;
        }
        object.put("visible", visible);

        object.put("label", label);
        object.put("description", Strings.nullToEmpty(description));

        if (owner) {
            object.put("owner", owner);
        }
        if (published) {
            object.put("published", published);
        }
        if (owner && pendingTransfer) {
            object.put("pendingTransfer", pendingTransfer);
        }
        if (suspended) {
            object.put("suspended", suspended);
        }

        object.put("resources", Json.toJsonArray(resources.values()));
        object.put("grants", Json.toJsonArray(grants.values()));
        object.put("locks", Json.toJsonArray(locks.values()));

        return object;
    }

    public static UserDatabaseMeta fromJson(JsonValue object) {
        UserDatabaseMeta meta = new UserDatabaseMeta();

        meta.databaseId = ResourceId.valueOf(object.getString("databaseId"));
        meta.userId = (int) object.getNumber("userId");
        meta.version = object.getString("version");

        meta.label = object.getString("label");
        meta.description = object.getString("description");

        if (object.hasKey("deleted") && object.get("deleted").asBoolean()) {
            meta.deleted = object.get("deleted").asBoolean();
            return meta;
        }
        meta.visible = object.getBoolean("visible");

        if (object.hasKey("owner")) {
            meta.owner = object.getBoolean("owner");
        }
        if (object.hasKey("published")) {
            meta.published = object.getBoolean("published");
        }
        if (meta.owner && object.hasKey("pendingTransfer")) {
            meta.pendingTransfer = object.getBoolean("pendingTransfer");
        }
        if (object.hasKey("suspended")) {
            meta.suspended = object.getBoolean("suspended");
        }

        JsonValue resourceArray = object.get("resources");
        for (int i = 0; i < resourceArray.length(); i++) {
            Resource resource = Resource.fromJson(resourceArray.get(i));
            meta.resources.put(resource.getId(), resource);
        }
        JsonValue grantsArray = object.get("grants");
        for (int i = 0; i < grantsArray.length(); i++) {
            GrantModel grant = GrantModel.fromJson(grantsArray.get(i));
            meta.grants.put(grant.getResourceId(), grant);
        }
        JsonValue lockArray = object.get("locks");
        for (int i = 0; i < lockArray.length(); i++) {
            RecordLock lock = RecordLock.fromJson(lockArray.get(i));
            meta.locks.put(lock.getResourceId(), lock);
        }

        meta.resourceNodeMap.putAll(constructResourceNodeMap(meta));

        return meta;
    }

    public static UserDatabaseMeta buildDeletedUserDatabaseMeta(@NotNull DatabaseMeta databaseMeta, int userId) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseMeta.getDatabaseId())
                .setUserId(userId)
                .setOwner(databaseMeta.getOwnerId() == userId)
                .setVersion(Long.toString(databaseMeta.getVersion()))
                .setLabel(databaseMeta.getLabel())
                .setDescription(databaseMeta.getDescription())
                .setDeleted(true)
                .build();
    }

    public static UserDatabaseMeta buildOwnedUserDatabaseMeta(@NotNull DatabaseMeta databaseMeta) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseMeta.getDatabaseId())
                .setUserId(databaseMeta.getOwnerId())
                .setOwner(true)
                .setLabel(databaseMeta.getLabel())
                .setDescription(databaseMeta.getDescription())
                .setPublished(databaseMeta.isPublished())
                .setVersion(Long.toString(databaseMeta.getVersion()))
                .setPendingTransfer(databaseMeta.isPendingTransfer())
                .addResources(databaseMeta.getResources().values())
                .addLocks(databaseMeta.getLocks().values())
                .setSuspended(databaseMeta.isSuspended())
                .build();
    }

    public static UserDatabaseMeta buildUserDatabaseMeta(@NotNull DatabaseGrant databaseGrant, @NotNull DatabaseMeta databaseMeta) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseMeta.getDatabaseId())
                .setUserId(databaseGrant.getUserId())
                .setOwner(databaseGrant.getUserId() == databaseMeta.getOwnerId())
                .setLabel(databaseMeta.getLabel())
                .setDescription(databaseMeta.getDescription())
                .setPublished(databaseMeta.isPublished())
                .setVersion(version(databaseMeta.getVersion(), databaseGrant.getVersion()))
                .addResources(databaseMeta.getResources().values())
                .addLocks(databaseMeta.getLocks().values())
                .addGrants(databaseGrant.getGrants().values())
                .setSuspended(databaseMeta.isSuspended())
                .build();
    }

    public static UserDatabaseMeta buildGrantlessUserDatabaseMeta(@NotNull DatabaseMeta databaseMeta, int userId) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseMeta.getDatabaseId())
                .setUserId(userId)
                .setLabel(databaseMeta.getLabel())
                .setDescription(databaseMeta.getDescription())
                .setPublished(databaseMeta.isPublished())
                .setVersion(version(databaseMeta.getVersion(),0))
                .addResources(databaseMeta.getResources().values())
                .addLocks(databaseMeta.getLocks().values())
                .setSuspended(databaseMeta.isSuspended())
                .build();
    }

    private static String version(long databaseVersion, long grantVersion) {
        return Long.toString(databaseVersion) + UserDatabaseMeta.VERSION_SEP + Long.toString(grantVersion);
    }

    private static Map<ResourceId,Resource.Node> constructResourceNodeMap(UserDatabaseMeta meta) {
        Map<ResourceId,Resource.Node> resourceNodeMap = Maps.newHashMap();
        if (meta.getResources().isEmpty()) {
            return resourceNodeMap;
        }
        meta.getResources().forEach(resource -> buildNode(resource, resourceNodeMap));
        meta.getResources().forEach(resource -> mapNode(resource, resourceNodeMap));
        return resourceNodeMap;
    }

    private static void buildNode(Resource resource, Map<ResourceId,Resource.Node> resourceNodeMap) {
        // Parent and Child Nodes will be set during mapNodes()
        Resource.Node node = new Resource.Node(resource);
        resourceNodeMap.put(resource.getId(), node);
    }

    private static void mapNode(Resource resource, Map<ResourceId,Resource.Node> resourceNodeMap) {
        Resource.Node node = resourceNodeMap.get(resource.getId());
        node.setParentNode(resourceNodeMap.get(resource.getParentId()));
        if (!node.isRoot()) {
            node.getParentNode().addChildNode(node);
        }
    }

    public static class Builder {

        private final UserDatabaseMeta meta = new UserDatabaseMeta();

        public Builder() {
            meta.version = "0";
        }

        public Builder setDatabaseId(@NotNull ResourceId id) {
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

        public Builder setVersion(@NotNull String version) {
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
                meta.grants.put(grant.getResourceId(), grant);
            }
            return this;
        }

        public Builder addLock(@NotNull RecordLock lock) {
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

        public Builder addResource(@NotNull Resource resource) {
            meta.resources.put(resource.getId(), resource);
            return this;
        }

        public UserDatabaseMeta build() {
            meta.visible = isVisible();

            // If not visible to current user, strip all non-visible data before building
            if (!meta.visible) {
                removeNonVisibleData();
                return meta;
            }

            // If database is visible but user is not owner, they will be restricted by their assigned grants (if any)
            if (!meta.owner) {
                if (meta.grants.isEmpty()) {
                    // If user has no explicit grants, then only PUBLIC resources are visible
                    removePrivateResources();
                } else {
                    // If user has explicit grants, then remove any resources which the user has not been granted rights to
                    removeNonGrantedResources();
                }
            }

            // Construct a resource node map from the remaining resources
            meta.resourceNodeMap.putAll(constructResourceNodeMap(meta));

            return meta;
        }

        private void removeNonGrantedResources() {
            Map<ResourceId,Resource.Node> resourceNodeMap = constructResourceNodeMap(meta);
            Set<ResourceId> grantedResources = new HashSet<>(resourceNodeMap.size());

            addExplicitlyGrantedResources(grantedResources, resourceNodeMap);
            addPublicResources(grantedResources);

            purgeNonGrantedResources(grantedResources);
        }

        private void addExplicitlyGrantedResources(Set<ResourceId> grantedResources, Map<ResourceId, Resource.Node> resourceNodeMap) {
            meta.grants.keySet().forEach(grantedResource -> addGrantedResources(grantedResource, grantedResources, resourceNodeMap));
        }

        private void addGrantedResources(ResourceId grantedResource,
                                         Set<ResourceId> grantedResources,
                                         Map<ResourceId, Resource.Node> resourceNodeMap) {

            // Check for a root database grant
            if (grantedResource.equals(meta.databaseId)) {
                // Root database grant - add all resources
                grantedResources.addAll(resourceNodeMap.values().stream()
                        .map(Resource.Node::getResource)
                        .map(Resource::getId)
                        .collect(Collectors.toSet()));
                return;
            }

            // Skip if resource is missing
            if (!resourceNodeMap.containsKey(grantedResource)) {
                return;
            }

            // Add the granted resource
            Resource.Node grantedResourceNode = resourceNodeMap.get(grantedResource);
            grantedResources.add(grantedResourceNode.getResource().getId());

            // Add any child resources
            grantedResourceNode.getChildNodes().stream()
                    .map(Resource.Node::getResource)
                    .map(Resource::getId)
                    .forEach(childId -> addGrantedResources(childId, grantedResources, resourceNodeMap));
        }

        private void addPublicResources(Set<ResourceId> grantedResources) {
            // Add any resources which are visible PUBLICLY or to DATABASE_USERS
            meta.resources.entrySet().stream()
                    .filter(resource -> resource.getValue().getVisibility() != Resource.Visibility.PRIVATE)
                    .map(Map.Entry::getKey)
                    .forEach(grantedResources::add);
        }

        private void purgeNonGrantedResources(Set<ResourceId> grantedResources) {
            meta.resources.entrySet().removeIf(resource -> !grantedResources.contains(resource.getKey()));
        }

        private boolean isVisible() {
            if (meta.deleted) {
                return false;
            }
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

        private void removeNonVisibleData() {
            // Strip all resources, grants and lock data as well as label and description
            meta.label = "";
            meta.description = "";
            meta.resources.clear();
            meta.grants.clear();
            meta.locks.clear();
        }

        private void removePrivateResources() {
            meta.resources.values().removeIf(resource -> !resource.isPublic());
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        // Convert both to JSON and compare -> must be _identical_
        return toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        return this.toJson().toJson();
    }
}
