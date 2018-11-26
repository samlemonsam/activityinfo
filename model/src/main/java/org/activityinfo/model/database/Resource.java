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
import java.util.ArrayList;
import java.util.List;

public class Resource implements JsonSerializable {

    enum Publicity {
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

    static class Node {

        private Node parentNode;
        private Resource resource;
        private List<Node> childNodes = new ArrayList<>(0);

        public Node(Node parentNode, Resource resource) {
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

        public Node getParentNode() {
            return parentNode;
        }

        public Resource getResource() {
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
    private Publicity publicity = Publicity.PRIVATE;

    private Resource() {}

    public Resource(ResourceId id, ResourceId parentId, String label) {
        this.id = id;
        this.parentId = parentId;
        this.label = label;
    }

    public Resource(ResourceId id, ResourceId parentId, String label, Publicity publicity) {
        this(id, parentId, label);
        this.publicity = publicity;
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

    public Publicity getPublicity() {
        return publicity;
    }

    public boolean isPublic() {
        return publicity == Publicity.PUBLIC;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", id.asString());
        object.put("parentId", parentId.asString());
        object.put("label", label);
        object.put("type", type.name());
        object.put("publicity", publicity.name());
        return object;
    }

    public static Resource fromJson(JsonValue object) {
        Resource resource = new Resource();
        resource.id = ResourceId.valueOf(object.getString("id"));
        resource.parentId = ResourceId.valueOf(object.getString("parentId"));
        resource.label = object.getString("label");
        resource.type = ResourceType.valueOf(object.getString("type"));
        resource.publicity = Publicity.valueOf(object.getString("publicity"));
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

        public Builder setPublic(boolean isPublic) {
            if (isPublic) {
                resource.publicity = Publicity.PUBLIC;
            } else {
                resource.publicity = Publicity.PRIVATE;
            }
            return this;
        }

        public Resource build() {
            assert resource.id != null : "id is missing";
            assert resource.label != null : "label is missing";
            assert resource.type != null : "type is missing";
            return resource;
        }
    }

}
