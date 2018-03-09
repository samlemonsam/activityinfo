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
package org.activityinfo.model.type.enumerated;

import com.google.common.base.Strings;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;

import static org.activityinfo.json.Json.createObject;

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


    public JsonValue toJsonObject() {
        JsonValue jsonObject = createObject();
        jsonObject.put("id", id.asString());
        jsonObject.put("label", label);
        if(!Strings.isNullOrEmpty(code)) {
            jsonObject.put("code", code);
        }
        return jsonObject;
    }
    
    public static EnumItem fromJsonObject(JsonValue jsonObject) {
        EnumItem enumItem = new EnumItem();
        enumItem.setId(ResourceId.valueOf(jsonObject.get("id").asString()));
        if(jsonObject.hasKey("label")) {
            enumItem.setLabel(jsonObject.get("label").asString());
        }
        if(jsonObject.hasKey("code")) {
            enumItem.setCode(jsonObject.get("code").asString());
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
