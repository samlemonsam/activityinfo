package org.activityinfo.model.analysis;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.JsonValue;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Analysis {
    private String id;
    private String parentId;
    private String label;
    private String modelType;
    private JsonValue model;

    public Analysis() {
    }

    @JsOverlay
    public static Analysis of(TypedAnalysis<?> model) {
        Analysis analysis = new Analysis();
        analysis.id = model.getId();
        analysis.parentId = model.getParentId();
        analysis.label = model.getLabel();
        analysis.modelType = model.getModel().getTypeId();
        analysis.model = model.getModel().toJson();
        return analysis;
    }

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
    public String getLabel() {
        return label;
    }

    @JsOverlay
    public void setLabel(String label) {
        this.label = label;
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
    public String getModelType() {
        return modelType;
    }

    @JsOverlay
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
}
