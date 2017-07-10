package org.activityinfo.model.analysis;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.JsonObject;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class AnalysisUpdate {

    private String id;
    private String folderId;
    private String type;
    private JsonObject model;

    @JsOverlay
    public String getId() {
        return id;
    }

    @JsOverlay
    public void setId(String id) {
        this.id = id;
    }

    @JsOverlay
    public String getFolderId() {
        return folderId;
    }

    @JsOverlay
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    @JsOverlay
    public String getType() {
        return type;
    }

    @JsOverlay
    public void setType(String type) {
        this.type = type;
    }

    @JsOverlay
    public JsonObject getModel() {
        return model;
    }

    @JsOverlay
    public void setModel(JsonObject model) {
        this.model = model;
    }
}
