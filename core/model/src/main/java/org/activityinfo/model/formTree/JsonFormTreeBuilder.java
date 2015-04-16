package org.activityinfo.model.formTree;

import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes/deserializes a FormTree to JSON
 */
public class JsonFormTreeBuilder {

    public static JsonObject toJson(FormTree tree)  {

        ResourceId rootFormClassId = Iterables.getOnlyElement(tree.getRootFormClasses().keySet());

        JsonObject forms = new JsonObject();
        collectForms(forms, tree.getRootFields());

        JsonObject object = new JsonObject();
        object.addProperty("root", rootFormClassId.asString());
        object.add("forms", forms);

        return object;
    }
    
    public static FormTree fromJson(JsonObject object) {
       
        ResourceId rootFormClassId = ResourceId.valueOf(object.getAsJsonPrimitive("root").getAsString());
       
        JsonObject forms = object.getAsJsonObject("forms");
        final Map<ResourceId, FormClass> formMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : forms.entrySet()) {
            FormClass formClass = FormClass.fromResource(Resources.fromJson(entry.getValue().getAsJsonObject()));
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
            if(!forms.has(formClass.getId().asString())) {
                forms.add(formClass.getId().asString(), Resources.toJsonObject(formClass.asResource()));
            }
            if(node.hasChildren()) {
                collectForms(forms, node.getChildren());
            }
        }
    }


}
