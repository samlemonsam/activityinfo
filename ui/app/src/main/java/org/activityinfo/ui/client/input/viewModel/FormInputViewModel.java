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
    private FormInputModel inputModel;
    private final Map<ResourceId, FieldValue> fieldValueMap;
    private final Map<ResourceId, RepeatingSubFormViewModel> repeatingSubFormMap;
    private final Map<ResourceId, KeyedSubFormViewModel> keyedSubFormMap;
    private final Set<ResourceId> relevant;
    private final Set<ResourceId> missing;
    private final Multimap<ResourceId, String> validationErrors;
    private final boolean valid;
    private boolean dirty;

    FormInputViewModel(FormTree formTree,
                       FormInputModel inputModel,
                       Map<ResourceId, FieldValue> fieldValueMap,
                       Map<ResourceId, RepeatingSubFormViewModel> repeatingSubFormMap,
                       Map<ResourceId, KeyedSubFormViewModel> keyedSubFormMap,
                       Set<ResourceId> relevant,
                       Set<ResourceId> missing,
                       Multimap<ResourceId, String> validationErrors, boolean valid, boolean dirty) {
        this.formTree = formTree;
        this.inputModel = inputModel;
        this.fieldValueMap = fieldValueMap;
        this.repeatingSubFormMap = repeatingSubFormMap;
        this.keyedSubFormMap = keyedSubFormMap;
        this.relevant = relevant;
        this.missing = missing;
        this.validationErrors = validationErrors;
        this.valid = valid;
        this.dirty = dirty;
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

    public RepeatingSubFormViewModel getRepeatingSubFormField(ResourceId fieldId) {
        return repeatingSubFormMap.get(fieldId);
    }

    public KeyedSubFormViewModel getKeyedSubFormField(ResourceId fieldId) {
        return keyedSubFormMap.get(fieldId);
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
            }
            if(newInput.getState() == FieldInput.State.EMPTY) {
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

        for (RepeatingSubFormViewModel repeatingSubFormViewModel : repeatingSubFormMap.values()) {
            tx.add(repeatingSubFormViewModel.buildUpdates(inputModel.getRecordRef()));
        }

        for (KeyedSubFormViewModel keyedSubFormViewModel : keyedSubFormMap.values()) {
            tx.add(keyedSubFormViewModel.buildUpdates(inputModel.getRecordRef()));
        }

        return tx.build();
    }

}
