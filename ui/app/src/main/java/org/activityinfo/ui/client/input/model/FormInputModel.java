package org.activityinfo.ui.client.input.model;


import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;

import java.util.HashMap;
import java.util.Map;

public class FormInputModel {

    private FormTree tree;
    private Map<ResourceId, InputModel> inputMap = new HashMap<>();

    public FormInputModel(FormTree tree) {
        this.tree = tree;

        // First create value cells for fixed fields
        for (FormTree.Node node : tree.getRootFields()) {
            if(!node.isCalculated() && !node.isSubForm()) {
                inputMap.put(node.getFieldId(), node.getType().accept(InputModelFactory.INSTANCE));
            }
        }
    }

    public InputModel getInputModel(ResourceId fieldId) {
        return inputMap.get(fieldId);
    }
}
