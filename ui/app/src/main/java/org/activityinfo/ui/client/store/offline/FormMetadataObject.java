package org.activityinfo.ui.client.store.offline;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormPermissions;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class FormMetadataObject {

    private String version;
    private JsonValue permissions;

    public FormMetadataObject() {
    }

    @JsOverlay
    public static FormMetadataObject from(FormMetadata metadata) {
        FormMetadataObject object = new FormMetadataObject();
        object.version = Long.toString(metadata.getVersion());
        object.permissions = metadata.getPermissions().toJson();
        return object;
    }

    @JsOverlay
    public long getVersion() {
        return Long.parseLong(version);
    }


    @JsOverlay
    public FormPermissions getPermissions() {
        return FormPermissions.fromJson(permissions);
    }


}
