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
package org.activityinfo.model.formTree;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes/deserializes a FormTree to JSON
 */
public class JsonFormTreeBuilder {

    private static final int LATEST_VERSION = 4001;

    public static JsonValue toJson(FormTree formTree) {
        return toJson(LATEST_VERSION, formTree);
    }

    public static JsonValue toJson(int clientVersion, FormTree formTree) {

        JsonValue forms = Json.createObject();
        for (FormMetadata formMetadata : formTree.getForms()) {
            if(clientVersion >= LATEST_VERSION) {
                forms.put(formMetadata.getId().asString(), formMetadata.toJson());
            } else {
                // Older clients are expecting only the form schema, not the full metadata
                if(formMetadata.isVisible()) {
                    forms.put(formMetadata.getId().asString(), formMetadata.getSchema().toJson());
                }
            }
        }

        JsonValue object = Json.createObject();
        object.put("root", formTree.getRootFormId().asString());
        object.put("forms", forms);

        return object;
    }

    public static List<FormMetadata> fromJsonAsList(JsonValue object) {
        JsonValue formObject = object.get("forms");
        List<FormMetadata> forms = new ArrayList<>();
        for (Map.Entry<String, JsonValue> form : formObject.entrySet()) {
            forms.add(FormMetadata.fromJson(form.getValue()));
        }
        return forms;
    }

    public static FormTree fromJson(JsonValue object) {
        Map<ResourceId, FormMetadata> map = new HashMap<>();
        for (FormMetadata formMetadata : fromJsonAsList(object)) {
            map.put(formMetadata.getId(), formMetadata);
        }
        FormMetadataProvider provider = new FormMetadataProvider() {
            @Override
            public FormMetadata getFormMetadata(ResourceId formId) {
                FormMetadata form = map.get(formId);
                if(form == null) {
                    return FormMetadata.notFound(formId);
                } else {
                    return form;
                }
            }
        };
        FormTreeBuilder builder = new FormTreeBuilder(provider);
        ResourceId rootFormId = ResourceId.valueOf(object.getString("root"));
        return builder.queryTree(rootFormId);
    }

}
