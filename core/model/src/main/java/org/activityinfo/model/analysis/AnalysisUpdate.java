package org.activityinfo.model.analysis;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.JsonValue;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class AnalysisUpdate {

    private String id;
    private String parentId;
    private String type;
    private String label;
    private JsonValue model;

    @JsOverlay
    public String getId() {
        return id;
    }

    @JsOverlay
    public void setId(String id) {
        this.id = id;
    }

    @JsOverlay
    public String getParentId() {
        return parentId;
    }

    @JsOverlay
    public void setParentId(String parentId) {
        this.parentId = parentId;
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
    public JsonValue getModel() {
        return model;
    }

    @JsOverlay
    public void setModel(JsonValue model) {
        this.model = model;
    }

    @JsOverlay
    public String getLabel() {
        return label;
    }

    @JsOverlay
    public void setLabel(String title) {
        this.label = title;
    }
}
