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
package org.activityinfo.model.form;

import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collections;
import java.util.List;

import static org.activityinfo.json.Json.createObject;

/**
 * Provides user-specific metadata for a given form, including permissions
 * and versioning.
 */
public class FormMetadata implements JsonSerializable {

    private ResourceId id;


    /**
     * The overall version of the form. The version number is incremented
     * whenever a record or the schema is changed.
     */
    private long version;

    /**
     * The version of the Schema. The version number is incremented
     * whenver a
     */
    private long schemaVersion;

    /**
     * True if this form has been deleted.
     */
    private boolean deleted = false;

    /**
     * The permissions for the current user.
     */
    private FormPermissions permissions;

    private FormClass schema;

    private boolean visible = true;


    public static FormMetadata notFound(ResourceId formId) {
        FormMetadata metadata = new FormMetadata();
        metadata.id = formId;
        metadata.visible = false;
        metadata.deleted = true;
        metadata.permissions = FormPermissions.none();
        return metadata;
    }

    public static FormMetadata forbidden(ResourceId formId) {
        FormMetadata metadata = new FormMetadata();
        metadata.id = formId;
        metadata.visible = false;
        metadata.permissions = FormPermissions.none();
        return metadata;
    }

    public static FormMetadata of(long version, FormClass schema, FormPermissions permissions) {
        FormMetadata metadata = new FormMetadata();
        metadata.id = schema.getId();
        metadata.version = version;
        metadata.visible = true;
        metadata.permissions = permissions;
        metadata.schema = schema;
        metadata.schemaVersion = schema.getSchemaVersion();
        return metadata;
    }

    public ResourceId getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public FormPermissions getPermissions() {
        return permissions;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isAccessible() {
        return visible && !deleted;
    }

    public FormClass getSchema() {
        assert visible : "form is not visible to user";
        assert !deleted : "form has been deleted.";
        return schema;
    }

    public void setSchema(FormClass schema) {
        this.schema = schema;
        this.schemaVersion = schema.getSchemaVersion();
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = createObject();
        object.put("id", id.asString());
        if(!visible) {
            object.put("visible", false);
        }
        if(deleted) {
            object.put("deleted", true);
        }
        if(schema != null) {
            object.put("schema", schema.toJson());
        }
        if(visible) {
            object.put("version", version);
            object.put("schemaVersion", schemaVersion);
            object.put("permissions", permissions.toJson());
        }
        return object;
    }

    public static FormMetadata fromJson(JsonValue object) {
        FormMetadata metadata = new FormMetadata();
        metadata.id = ResourceId.valueOf(object.get("id").asString());

        if(object.hasKey("version")) {
            metadata.version = object.get("version").asLong();
        }
        if(object.hasKey("schemaVersion")) {
            metadata.schemaVersion = object.get("schemaVersion").asLong();
        }
        if(object.hasKey("schema")) {
            metadata.schema = FormClass.fromJson(object.get("schema"));
        }
        if(object.hasKey("permissions")) {
            metadata.permissions = FormPermissions.fromJson(object.get("permissions"));
        }
        if(object.hasKey("visible")) {
            metadata.visible = object.get("visible").asBoolean();
        }
        if(object.hasKey("deleted")) {
            metadata.deleted = object.get("deleted").asBoolean();
        }
        return metadata;
    }

    public List<FormField> getFields() {
        if(isVisible()) {
            return getSchema().getFields();
        } else {
            return Collections.emptyList();
        }
    }
}
