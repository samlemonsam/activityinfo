package org.activityinfo.ui.client.input.viewModel;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import org.activityinfo.json.Json;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;

import java.util.Collection;
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
    private Map<ResourceId, FieldValue> existingValues;
    private FormInputModel inputModel;
    private final Map<ResourceId, FieldValue> fieldValueMap;
    private final Map<ResourceId, SubFormViewModel> subFormMap;
    private final Set<ResourceId> relevant;
    private final Set<ResourceId> missing;
    private final Multimap<ResourceId, String> validationErrors;
    private final boolean valid;
    private boolean dirty;
    private boolean placeholder;

    FormInputViewModel(FormTree formTree,
                       Map<ResourceId, FieldValue> existingValues,
                       FormInputModel inputModel,
                       Map<ResourceId, FieldValue> fieldValueMap,
                       Map<ResourceId, SubFormViewModel> subFormMap,
                       Set<ResourceId> relevant,
                       Set<ResourceId> missing,
                       Multimap<ResourceId, String> validationErrors,
                       boolean valid,
                       boolean dirty,
                       boolean placeholder) {
        this.formTree = formTree;
        this.existingValues = existingValues;
        this.inputModel = inputModel;
        this.fieldValueMap = fieldValueMap;
        this.subFormMap = subFormMap;
        this.relevant = relevant;
        this.missing = missing;
        this.validationErrors = validationErrors;
        this.valid = valid;
        this.dirty = dirty;
        this.placeholder = placeholder;
    }

    public FormTree getFormTree() {
        return formTree;
    }

    public FormInputModel getInputModel() {
        return inputModel;
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

    public boolean isDirty() {
        return dirty;
    }

    public boolean isEmpty() {
        return fieldValueMap.isEmpty();
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public SubFormViewModel getSubForm(ResourceId fieldId) {
        return subFormMap.get(fieldId);
    }

    public boolean isMissing(ResourceId fieldId) {
        return missing.contains(fieldId);
    }

    public FieldValue getField(ResourceId fieldId) {
        return fieldValueMap.get(fieldId);
    }

    public RecordUpdate buildUpdate(Optional<RecordRef> parentRef) {
        RecordUpdate update = new RecordUpdate();
        update.setRecordId(inputModel.getRecordRef().getRecordId());
        update.setFormId(inputModel.getRecordRef().getFormId());

        if(parentRef.isPresent()) {
            update.setParentRecordId(parentRef.get().getRecordId().asString());
        }

        for (FormTree.Node node : formTree.getRootFields()) {
            FieldInput newInput = inputModel.get(node.getFieldId());

            if(newInput.getState() == FieldInput.State.VALID) {

                update.setFieldValue(node.getFieldId(), newInput.getValue());

            } else if(existingValues.containsKey(node.getFieldId()) &&
                    newInput.getState() == FieldInput.State.EMPTY) {

                update.setFieldValue(node.getFieldId().asString(), Json.createNull());

            } else if(existingValues.containsKey(node.getFieldId()) &&
                    !relevant.contains(node.getFieldId())) {

                update.setFieldValue(node.getFieldId().asString(), Json.createNull());
            }
        }
        return update;
    }

    public Collection<String> getValidationErrors(ResourceId fieldId) {
        return validationErrors.get(fieldId);
    }

    public RecordTransaction buildTransaction() {
        RecordTransactionBuilder tx = new RecordTransactionBuilder();
        tx.add(buildUpdate(Optional.absent()));

        for (SubFormViewModel subFormViewModel : subFormMap.values()) {
            tx.add(subFormViewModel.buildUpdates(inputModel.getRecordRef()));
        }
        return tx.build();
    }

}
