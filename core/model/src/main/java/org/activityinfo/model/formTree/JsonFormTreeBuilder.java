package org.activityinfo.model.formTree;

import org.activityinfo.json.JsonObject;
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

    public static org.activityinfo.json.JsonObject toJson(FormTree tree)  {

        ResourceId rootFormClassId = tree.getRootFormId();

        JsonObject forms = createObject();
        collectForms(forms, tree.getRootFields());

        JsonObject object = createObject();
        object.put("root", rootFormClassId.asString());
        object.put("forms", forms);

        return object;
    }
    
    public static FormTree fromJson(JsonObject object) {
       
        ResourceId rootFormClassId = ResourceId.valueOf(object.get("root").asString());
       
        JsonObject forms = object.getObject("forms");
        final Map<ResourceId, FormClass> formMap = new HashMap<>();
        for (String key : forms.keys()) {
            JsonValue value = forms.get(key);
            FormClass formClass = FormClass.fromJson(value.getAsJsonObject());
            formMap.put(formClass.getId(), formClass);
        }

        FormClassProvider provider = new FormClassProvider() {
            @Override
            public FormClass getFormClass(ResourceId resourceId) {
                FormClass formClass = formMap.get(resourceId);
                assert formClass != null : "FormClass " + resourceId + " was referenced but not include in the " +
                        "list of forms";
                return formClass;
            }
        };
        FormTreeBuilder builder = new FormTreeBuilder(provider);
        return builder.queryTree(rootFormClassId);
    }

    private static void collectForms(JsonObject forms, List<FormTree.Node> nodes) {
        for (FormTree.Node node : nodes) {
            FormClass formClass = node.getDefiningFormClass();
            if(!forms.hasKey(formClass.getId().asString())) {
                forms.put(formClass.getId().asString(), formClass.toJsonObject());
            }
            if(node.hasChildren()) {
                collectForms(forms, node.getChildren());
            }
        }
    }


}
