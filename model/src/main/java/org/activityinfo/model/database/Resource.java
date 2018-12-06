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

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class Resource implements JsonSerializable {

    public enum Visibility {
        /**
         * Public. Visible to *all* Users on system.
         */
        PUBLIC,

        /**
         * Public to Database Users. Visible only to Users currently defined on Database.
         */
        DATABASE_USERS,

        /**
         * Private. Visible only to Users given explicit permissions.
         */
        PRIVATE
    }

    public static class Node {

        private Node parentNode;
        private Resource resource;
        private List<Node> childNodes = new ArrayList<>(0);

        public Node(@NotNull Resource resource) {
            this.resource = resource;
        }

        public Node(@Nullable Node parentNode, @NotNull Resource resource) {
            this.parentNode = parentNode;
            this.resource = resource;
        }

        public boolean isRoot() {
            return parentNode == null;
        }

        public boolean isLeaf() {
            return childNodes.isEmpty();
        }

        public void addChildNode(Node childNode) {
            childNodes.add(childNode);
        }

        public void addChildNodes(List<Node> children) {
            childNodes.addAll(children);
        }

        public @Nullable Node getParentNode() {
            return parentNode;
        }

        public void setParentNode(Node parentNode) {
            this.parentNode = parentNode;
        }

        public @NotNull Resource getResource() {
            return resource;
        }

        public List<Node> getChildNodes() {
            return childNodes;
        }
    }

    private ResourceId id;
    private ResourceId parentId;
    private ResourceType type;
    private String label;
    private Visibility visibility = Visibility.PRIVATE;

    private Resource() {}

    public Resource(ResourceId id, ResourceId parentId, String label) {
        this.id = id;
        this.parentId = parentId;
        this.label = label;
    }

    public Resource(ResourceId id, ResourceId parentId, String label, Visibility visibility) {
        this(id, parentId, label);
        this.visibility = visibility;
    }

    public ResourceId getId() {
        return id;
    }

    public ResourceId getParentId() {
        return parentId;
    }

    public String getLabel() {
        return label;
    }

    public ResourceType getType() {
        return type;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean isPublic() {
        return visibility == Visibility.PUBLIC;
    }

    public boolean isPublicToDatabaseUsers() {
        return visibility == Visibility.DATABASE_USERS;
    }

    public boolean isPrivate() {
        return visibility == Visibility.PRIVATE;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", id.asString());
        object.put("parentId", parentId.asString());
        object.put("label", label);
        object.put("type", type.name());
        object.put("visibility", visibility.name());
        return object;
    }

    public static Resource fromJson(JsonValue object) {
        Resource resource = new Resource();
        resource.id = ResourceId.valueOf(object.getString("id"));
        resource.parentId = ResourceId.valueOf(object.getString("parentId"));
        resource.label = object.getString("label");
        resource.type = ResourceType.valueOf(object.getString("type"));
        resource.visibility = Visibility.valueOf(object.getString("visibility"));
        return resource;
    }

    public static class Builder {
        private Resource resource = new Resource();

        public Builder setId(ResourceId id) {
            resource.id = id;
            return this;
        }

        public Builder setLabel(@Nonnull String label) {
            resource.label = label;
            return this;
        }

        public Builder setParentId(ResourceId id) {
            resource.parentId = id;
            return this;
        }

        public Builder setType(ResourceType type) {
            resource.type = type;
            return this;
        }

        public Builder setVisibility(Visibility visibility) {
            resource.visibility = visibility;
            return this;
        }

        public Builder setVisibleToPublic() {
            return setVisibility(Visibility.PUBLIC);
        }

        public Builder setVisibleToDatabaseUser() {
            return setVisibility(Visibility.DATABASE_USERS);
        }

        public Builder setPrivate() {
            return setVisibility(Visibility.PRIVATE);
        }

        public Resource build() {
            assert resource.id != null : "id is missing";
            assert resource.label != null : "label is missing";
            assert resource.type != null : "type is missing";
            return resource;
        }
    }

}
