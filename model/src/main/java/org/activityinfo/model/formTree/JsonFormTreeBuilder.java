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
