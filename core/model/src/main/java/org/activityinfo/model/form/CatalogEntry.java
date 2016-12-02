package org.activityinfo.model.form;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a  Form or FormFolder 
 */
public class CatalogEntry {

    String id;
    String label;
    CatalogEntryType type;

    private CatalogEntry() {
    }

    public CatalogEntry(String id, String label, CatalogEntryType type) {
        this.id = id;
        this.label = label;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public CatalogEntryType getType() {
        return type;
    }
    
    public JsonElement toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("type", type.name().toLowerCase());
        jsonObject.addProperty("label", label);
        return jsonObject;
    }

    public static CatalogEntry fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        CatalogEntry model = new CatalogEntry();
        model.id = jsonObject.get("id").getAsString();
        model.type = CatalogEntryType.valueOf(jsonObject.get("type").getAsString().toUpperCase());
        model.label = jsonObject.get("label").getAsString();
        return model;
    }

    public static List<CatalogEntry> fromJsonArray(JsonArray jsonArray) {
        List<CatalogEntry> list = new ArrayList<>();
        for(JsonElement element : jsonArray) {
            list.add(fromJson(element));
        }
        return list;
    }
}
