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

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.activityinfo.json.Json.createObject;

/**
 * Serializes/deserializes a FormTree to JSON
 */
public class JsonFormTreeBuilder {

    public static JsonValue toJson(FormTree tree)  {

        ResourceId rootFormClassId = tree.getRootFormId();

        JsonValue forms = createObject();
        collectForms(forms, tree.getRootFields());

        JsonValue object = createObject();
        object.put("root", rootFormClassId.asString());
        object.put("forms", forms);

        return object;
    }
    
    public static FormTree fromJson(JsonValue object) {
       
        ResourceId rootFormClassId = ResourceId.valueOf(object.get("root").asString());
       
        JsonValue forms = object.get("forms");
        final Map<ResourceId, FormClass> formMap = new HashMap<>();
        for (String key : forms.keys()) {
            JsonValue value = forms.get(key);
            FormClass formClass = FormClass.fromJson(value);
            formMap.put(formClass.getId(), formClass);
        }

        FormClassProvider provider = new FormClassProvider() {
            @Override
            public FormClass getFormClass(ResourceId formId) {
                FormClass formClass = formMap.get(formId);
                assert formClass != null : "FormClass " + formId + " was referenced but not include in the " +
                        "list of forms";
                return formClass;
            }
        };
        FormTreeBuilder builder = new FormTreeBuilder(provider);
        return builder.queryTree(rootFormClassId);
    }

    private static void collectForms(JsonValue forms, List<FormTree.Node> nodes) {
        for (FormTree.Node node : nodes) {
            FormClass formClass = node.getDefiningFormClass();
            if(!forms.hasKey(formClass.getId().asString())) {
                forms.put(formClass.getId().asString(), formClass.toJson());
            }
            if(node.hasChildren()) {
                collectForms(forms, node.getChildren());
            }
        }
    }


}
