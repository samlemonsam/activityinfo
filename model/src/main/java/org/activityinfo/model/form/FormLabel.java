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
import org.activityinfo.model.resource.ResourceId;

import static org.activityinfo.json.Json.createObject;

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
    public JsonValue toJsonObject() {
        JsonValue object = createObject();
        object.put("id", id.asString());
        object.put("label", label);
        object.put("type", "label");
        object.put("visible", visible);
        return object;
    }

    public static FormElement fromJson(JsonValue jsonObject) {
        FormLabel label = new FormLabel(ResourceId.valueOf(jsonObject.get("id").asString()));
        label.setLabel(jsonObject.get("label").asString());
        if(jsonObject.hasKey("visible")) {
            label.setVisible(jsonObject.get("visible").asBoolean());
        }
        return label;
    }

}
