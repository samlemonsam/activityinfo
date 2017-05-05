package org.activityinfo.model.form;

import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;

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
        JsonObject object = new JsonObject();
        object.addProperty("id", id.asString());
        if(!visible) {
            object.addProperty("visible", false);
        }
        if(deleted) {
            object.addProperty("deleted", true);
        }
        if(schema != null) {
            object.add("schema", schema.toJsonObject());
        }
        if(visible) {
            object.addProperty("version", version);
            object.addProperty("schemaVersion", version);
        }
        return object;
    }

    public static FormMetadata fromJson(JsonObject object) {
        FormMetadata metadata = new FormMetadata();
        metadata.id = ResourceId.valueOf(object.getAsJsonPrimitive("id").getAsString());

        if(object.has("version")) {
            metadata.version = object.getAsJsonPrimitive("version").getAsLong();
        }
        if(object.has("schemaVersion")) {
            metadata.version = object.getAsJsonPrimitive("schemaVersion").getAsLong();
        }
        if(object.has("schema")) {
            metadata.schema = FormClass.fromJson(object.getAsJsonObject("schema"));
        }
        if(object.has("visible")) {
            metadata.visible = object.getAsJsonObject("visible").getAsBoolean();
        }
        if(object.has("deleted")) {
            metadata.visible = object.getAsJsonObject("deleted").getAsBoolean();
        }
        return metadata;
    }
}
