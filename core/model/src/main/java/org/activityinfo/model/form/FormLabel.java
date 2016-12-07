package org.activityinfo.model.form;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;

/**
 * Created by yuriyz on 4/15/2016.
 */
public class FormLabel extends FormElement {

    private final ResourceId id;
    private String label;
    private boolean visible = true;

    public FormLabel(ResourceId id) {
        this(id, null);
    }

    public FormLabel(ResourceId id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    public ResourceId getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public FormLabel setLabel(String label) {
        this.label = label;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public FormLabel setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public JsonElement toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id.asString());
        object.addProperty("label", label);
        object.addProperty("type", "label");
        object.addProperty("visible", visible);
        return object;
    }

    public static FormElement fromJson(JsonObject jsonObject) {
        FormLabel label = new FormLabel(ResourceId.valueOf(jsonObject.get("id").getAsString()));
        label.setLabel(jsonObject.get("label").getAsString());
        if(jsonObject.has("visible")) {
            label.setVisible(jsonObject.get("visible").getAsBoolean());
        }
        return label;
    }

}
