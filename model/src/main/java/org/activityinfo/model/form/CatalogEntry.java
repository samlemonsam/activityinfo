/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.model.form;

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
    boolean leaf;

    private CatalogEntry() {
    }

    public CatalogEntry(String id, String label, CatalogEntryType type) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.leaf = (type != CatalogEntryType.FOLDER);
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
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
        JsonValue jsonObject = createObject();
        jsonObject.put("id", id);
        jsonObject.put("type", type.name().toLowerCase());
        jsonObject.put("label", label);
        jsonObject.put("leaf", leaf);
        return jsonObject;
    }

    public static CatalogEntry fromJson(JsonValue jsonElement) {
        JsonValue jsonObject = jsonElement;
        CatalogEntry model = new CatalogEntry();
        model.id = jsonObject.get("id").asString();
        model.type = CatalogEntryType.valueOf(jsonObject.get("type").asString().toUpperCase());
        model.label = jsonObject.get("label").asString();
        model.leaf = jsonObject.getBoolean("leaf");
        return model;
    }

    public static List<CatalogEntry> fromJsonArray(JsonValue jsonArray) {
        List<CatalogEntry> list = new ArrayList<>();
        for(JsonValue element : jsonArray.values()) {
            list.add(fromJson(element));
        }
        return list;
    }
}
