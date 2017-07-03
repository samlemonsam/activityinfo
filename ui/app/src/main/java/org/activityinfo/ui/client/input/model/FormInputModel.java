package org.activityinfo.ui.client.input.model;


import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;

import java.util.*;

/**
 * The state of the user's input into the form as well any sub forms.
 * Immutable.
 */
public class FormInputModel {

    private final RecordRef recordRef;
    private final Map<ResourceId, FieldInput> fieldInputs;
    private final Map<RecordRef, FormInputModel> subRecords;

    public FormInputModel(RecordRef recordRef) {
        this.recordRef = recordRef;
        fieldInputs = Collections.emptyMap();
        subRecords = Collections.emptyMap();
    }

    private FormInputModel(RecordRef recordRef,
                           Map<ResourceId, FieldInput> fieldInputs,
                           Map<RecordRef, FormInputModel> subRecords) {
        this.recordRef = recordRef;
        this.fieldInputs = fieldInputs;
        this.subRecords = subRecords;
    }

    public FieldInput get(ResourceId fieldId) {
        FieldInput fieldInput = fieldInputs.get(fieldId);
        if(fieldInput == null) {
            return FieldInput.UNTOUCHED;
        }
        return fieldInput;
    }

    public RecordRef getRecordRef() {
        return recordRef;
    }

    /**
     * Returns a new, updated version of this model with the change to the given root field.
     */
    public FormInputModel update(ResourceId fieldId, FieldInput input) {
        return update(recordRef, fieldId, input);
    }

    public FormInputModel update(ResourceId fieldId, FieldValue value) {
        return update(recordRef, fieldId, new FieldInput(value));
    }

    /**
     * Returns a new, updated version of this model with the change to the given field on the given
     * record.
     *
     * @param recordRef the record to change, either the parent record or one of the sub records.
     * @param fieldId the id of the field to change
     * @param input the user's input.
     * @return a new copy of
     */
    public FormInputModel update(RecordRef recordRef, ResourceId fieldId, FieldInput input) {

        Map<ResourceId, FieldInput> updatedInputs = this.fieldInputs;
        Map<RecordRef, FormInputModel> updatedSubRecords = this.subRecords;

        if(recordRef.equals(this.recordRef)) {
            updatedInputs = new HashMap<>(this.fieldInputs);
            updatedInputs.put(fieldId, input);
        } else {
            updatedSubRecords = new HashMap<>(this.subRecords);
            FormInputModel subRecord = updatedSubRecords.get(recordRef);
            if(subRecord == null) {
                subRecord = new FormInputModel(recordRef);
            }
            updatedSubRecords.put(recordRef, subRecord.update(recordRef, fieldId, input));
        }

        return new FormInputModel(this.recordRef, updatedInputs, updatedSubRecords);
    }


    public FormInputModel addSubRecord(RecordRef newRecordRef) {
        assert !newRecordRef.equals(this.recordRef);

        if(subRecords.containsKey(newRecordRef)) {
            return this;
        }

        Map<RecordRef, FormInputModel> updatedSubRecords = new HashMap<>(this.subRecords);
        updatedSubRecords.put(newRecordRef, new FormInputModel(newRecordRef));

        return new FormInputModel(recordRef, fieldInputs, updatedSubRecords);
    }

    public Collection<FormInputModel> getSubRecords() {
        return subRecords.values();
    }

    public Optional<FormInputModel> getSubRecord(RecordRef recordRef) {
        return Optional.ofNullable(subRecords.get(recordRef));
    }



}
