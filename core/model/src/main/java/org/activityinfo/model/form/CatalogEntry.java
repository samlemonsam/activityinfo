package org.activityinfo.model.form;

import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.json.Json.createObject;

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
    
    public JsonValue toJsonElement() {
        JsonObject jsonObject = createObject();
        jsonObject.put("id", id);
        jsonObject.put("type", type.name().toLowerCase());
        jsonObject.put("label", label);
        return jsonObject;
    }

    public static CatalogEntry fromJson(JsonValue jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        CatalogEntry model = new CatalogEntry();
        model.id = jsonObject.get("id").asString();
        model.type = CatalogEntryType.valueOf(jsonObject.get("type").asString().toUpperCase());
        model.label = jsonObject.get("label").asString();
        return model;
    }

    public static List<CatalogEntry> fromJsonArray(JsonArray jsonArray) {
        List<CatalogEntry> list = new ArrayList<>();
        for(JsonValue element : jsonArray.values()) {
            list.add(fromJson(element));
        }
        return list;
    }
}
