package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.input.model.FieldInput;
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
    private final Map<ResourceId, ReferenceChoices> choices;
    private final boolean valid;

    FormInputViewModel(FormTree formTree,
                       FormInputModel inputModel,
                       Map<ResourceId, FieldValue> fieldValueMap,
                       Map<ResourceId, SubFormInputViewModel> subFormMap,
                       Set<ResourceId> relevant,
                       Set<ResourceId> missing,
                       Map<ResourceId, ReferenceChoices> choices, boolean valid) {
        this.formTree = formTree;
        this.inputModel = inputModel;
        this.fieldValueMap = fieldValueMap;
        this.subFormMap = subFormMap;
        this.relevant = relevant;
        this.missing = missing;
        this.choices = choices;
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

    public boolean isMissing(ResourceId fieldId) {
        return missing.contains(fieldId);
    }

    public FieldValue getField(ResourceId fieldId) {
        return fieldValueMap.get(fieldId);
    }

    public ReferenceChoices getChoices(ResourceId fieldId) {
        return choices.get(fieldId);
    }

    public RecordUpdate buildUpdate() {
        RecordUpdate update = new RecordUpdate();
        update.setRecordId(inputModel.getRecordRef().getRecordId());
        update.setFormId(inputModel.getRecordRef().getFormId());

        for (FormTree.Node node : formTree.getRootFields()) {
            FieldInput newInput = inputModel.get(node.getFieldId());
            if(newInput.getState() == FieldInput.State.VALID) {
                update.setFieldValue(node.getFieldId(), newInput.getValue());
            }
        }
        return update;
    }

    public RecordTransaction buildTransaction() {
        RecordTransactionBuilder tx = new RecordTransactionBuilder();
        tx.add(buildUpdate());

        for (SubFormInputViewModel subFormInputViewModel : subFormMap.values()) {
            tx.add(subFormInputViewModel.buildUpdates());
        }

        return tx.build();
    }
}
