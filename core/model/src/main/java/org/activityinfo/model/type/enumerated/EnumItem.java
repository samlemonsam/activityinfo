package org.activityinfo.model.type.enumerated;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;

public class EnumItem implements Serializable {
    private ResourceId id;
    private String label;
    private String code;

    private EnumItem() {
    }
    
    public EnumItem(ResourceId id, String label) {
        this.id = id;
        this.label = label;
    }

    public ResourceId getId() {
        return id;
    }

    public void setId(ResourceId id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    public JsonElement toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id.asString());
        jsonObject.addProperty("label", label);
        if(!Strings.isNullOrEmpty(code)) {
            jsonObject.addProperty("code", code);
        }
        return jsonObject;
    }
    
    public static EnumItem fromJsonObject(JsonObject jsonObject) {
        EnumItem enumItem = new EnumItem();
        enumItem.setId(ResourceId.valueOf(jsonObject.get("id").getAsString()));
        if(jsonObject.has("label")) {
            enumItem.setLabel(jsonObject.get("label").getAsString());
        }
        if(jsonObject.has("code")) {
            enumItem.setCode(jsonObject.get("code").getAsString());
        }
        return enumItem;
    }

    public String getCode() {
        return code;
    }

    public EnumItem setCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public String toString() {
        return id + ":" + label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnumItem enumItem = (EnumItem) o;

        if (id != null ? !id.equals(enumItem.id) : enumItem.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static ResourceId generateId() {
        KeyGenerator generator = new KeyGenerator();
        return CuidAdapter.attributeField(generator.generateInt());
    }

}
