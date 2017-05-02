package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.input.model.FormInputModel;

import java.util.Map;
import java.util.Set;

/**
 * View model of the {@link FormInputModel}.
 *
 * <p>This view model combines the users' input in {@code FormInputModel} with the form's schema to compute
 * an augmented model that includes relevancy status, field and form validity, and existing state.</p>
 */
public class FormInputViewModel {

    private FormTree formTree;
    private FormInputModel inputModel;
    private final Map<ResourceId, FieldValue> fieldValueMap;
    private final Map<ResourceId, SubFormInputViewModel> subFormMap;
    private final Set<ResourceId> relevant;
    private final Set<ResourceId> missing;
    private final boolean valid;

    FormInputViewModel(FormTree formTree,
                       FormInputModel inputModel,
                       Map<ResourceId, FieldValue> fieldValueMap,
                       Map<ResourceId, SubFormInputViewModel> subFormMap,
                       Set<ResourceId> relevant,
                       Set<ResourceId> missing,
                       boolean valid) {
        this.formTree = formTree;
        this.inputModel = inputModel;
        this.fieldValueMap = fieldValueMap;
        this.subFormMap = subFormMap;
        this.relevant = relevant;


        this.missing = missing;
        this.valid = valid;
    }

    public RecordRef getRecordRef() {
        return inputModel.getRecordRef();
    }

    public boolean isRelevant(ResourceId fieldId) {
        return relevant.contains(fieldId);
    }

    public boolean isValid() {
        return valid;
    }

    public SubFormInputViewModel getSubFormField(ResourceId fieldId) {
        return subFormMap.get(fieldId);
    }
}
