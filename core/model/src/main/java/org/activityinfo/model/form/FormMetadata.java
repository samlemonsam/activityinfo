package org.activityinfo.model.form;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonMappingException;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.resource.ResourceId;
import org.codehaus.jackson.FormatSchema;

import static org.activityinfo.json.Json.createObject;

/**
 * Provides user-specific metadata for a given form, including permissions
 * and versioning.
 */
public class FormMetadata {

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

    public JsonObject toJsonObject() {
        JsonObject object = createObject();
        object.put("id", id.asString());
        if(!visible) {
            object.put("visible", false);
        }
        if(deleted) {
            object.put("deleted", true);
        }
        if(schema != null) {
            object.put("schema", schema.toJsonObject());
        }
        if(visible) {
            object.put("version", version);
            object.put("schemaVersion", schemaVersion);
            object.put("permissions", permissions.toJson());
        }
        return object;
    }

    public static FormMetadata fromJson(JsonObject object) {
        FormMetadata metadata = new FormMetadata();
        metadata.id = ResourceId.valueOf(object.get("id").asString());

        if(object.hasKey("version")) {
            metadata.version = object.get("version").asLong();
        }
        if(object.hasKey("schemaVersion")) {
            metadata.schemaVersion = object.get("schemaVersion").asLong();
        }
        if(object.hasKey("schema")) {
            metadata.schema = FormClass.fromJson(object.get("schema").getAsJsonObject());
        }
        if(object.hasKey("permissions")) {
            metadata.permissions = FormPermissions.fromJson(object.getObject("permissions"));
        }
        if(object.hasKey("visible")) {
            metadata.visible = object.get("visible").asBoolean();
        }
        if(object.hasKey("deleted")) {
            metadata.deleted = object.get("deleted").asBoolean();
        }
        return metadata;
    }
}
