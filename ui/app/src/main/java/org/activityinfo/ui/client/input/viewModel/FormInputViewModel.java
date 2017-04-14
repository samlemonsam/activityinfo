package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.ui.client.input.model.FormInputModel;

import java.util.Map;
import java.util.Set;


public class FormInputViewModel {

    private FormTree formTree;
    private FormInputModel inputModel;
    private final Map<ResourceId, FieldValue> fieldValueMap;
    private final Map<ResourceId, Boolean> relevantMap;
    private final Set<ResourceId> missing;
    private final boolean valid;

    FormInputViewModel(FormTree formTree,
                       FormInputModel inputModel,
                       Map<ResourceId, FieldValue> fieldValueMap,
                       Map<ResourceId, Boolean> relevantMap, Set<ResourceId> missing,
                       boolean valid) {
        this.formTree = formTree;
        this.inputModel = inputModel;
        this.fieldValueMap = fieldValueMap;
        this.relevantMap = relevantMap;


        this.missing = missing;
        this.valid = valid;
    }

    public boolean isRelevant(ResourceId fieldId) {
        return relevantMap.get(fieldId) == Boolean.TRUE;
    }

    public boolean isValid() {
        return valid;
    }
}
