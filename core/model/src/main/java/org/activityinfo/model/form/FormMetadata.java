package org.activityinfo.model.form;

import org.activityinfo.json.JsonObject;
import org.activityinfo.model.resource.ResourceId;

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
     * True if the user has permission to see this form at all
     */
    private boolean visible = true;

    /**
     * True if this form has been deleted.
     */
    private boolean deleted = false;


    /**
     * A boolean-valued formula that determines whether a record is
     * visible to the current user
     */
    private String viewFilter;

    /**
     * A boolean-valued formula that determines whether a record is
     */
    private String editFilter;

    private FormClass schema;


    public ResourceId getId() {
        return id;
    }

    public void setId(ResourceId id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setSchemaVersion(long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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
            object.put("schemaVersion", version);
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
            metadata.version = object.get("schemaVersion").asLong();
        }
        if(object.hasKey("schema")) {
            metadata.schema = FormClass.fromJson(object.getObject("schema"));
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
