package org.activityinfo.ui.client.input.model;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
    private final Map<ResourceId, RecordRef> activeSubRecords;
    private final Set<RecordRef> deletedSubRecords;

    public FormInputModel(RecordRef recordRef) {
        this.recordRef = recordRef;
        fieldInputs = Collections.emptyMap();
        subRecords = Collections.emptyMap();
        activeSubRecords = Collections.emptyMap();
        deletedSubRecords = Collections.emptySet();
    }

    private FormInputModel(RecordRef recordRef,
                           Map<ResourceId, FieldInput> fieldInputs,
                           Map<RecordRef, FormInputModel> subRecords,
                           Map<ResourceId, RecordRef> activeSubRecords,
                           Set<RecordRef> deletedSubRecords) {
        this.recordRef = recordRef;
        this.fieldInputs = fieldInputs;
        this.subRecords = subRecords;
        this.activeSubRecords = activeSubRecords;
        this.deletedSubRecords = deletedSubRecords;
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


    public FormInputModel updateActiveSubRecord(ResourceId fieldId, RecordRef newActiveRef) {
        Map<ResourceId, RecordRef> updatedMap = Maps.newHashMap(this.activeSubRecords);
        RecordRef oldRef = updatedMap.put(fieldId, newActiveRef);
        if(Objects.equals(oldRef, newActiveRef)) {
            return this;
        }

        return new FormInputModel(recordRef, fieldInputs, subRecords, updatedMap, deletedSubRecords);
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

        return new FormInputModel(this.recordRef,
                updatedInputs,
                updatedSubRecords,
                activeSubRecords,
                deletedSubRecords);
    }

    public FormInputModel deleteSubRecord(RecordRef recordRef) {
        HashMap<RecordRef, FormInputModel> newSubRecords = Maps.newHashMap(this.subRecords);
        Set<RecordRef> newDeleted = Sets.newHashSet();

        newSubRecords.remove(recordRef);
        newDeleted.add(recordRef);

        return new FormInputModel(
                this.recordRef,
                fieldInputs,
                newSubRecords,
                activeSubRecords,
                newDeleted);
    }

    public FormInputModel addSubRecord(RecordRef newRecordRef) {
        assert !newRecordRef.equals(this.recordRef);

        if(subRecords.containsKey(newRecordRef)) {
            return this;
        }

        Map<RecordRef, FormInputModel> updatedSubRecords = new HashMap<>(this.subRecords);
        updatedSubRecords.put(newRecordRef, new FormInputModel(newRecordRef));

        return new FormInputModel(recordRef,
                fieldInputs,
                updatedSubRecords,
                activeSubRecords,
                deletedSubRecords);
    }

    public Collection<FormInputModel> getSubRecords() {
        return subRecords.values();
    }

    public Optional<FormInputModel> getSubRecord(RecordRef recordRef) {
        return Optional.ofNullable(subRecords.get(recordRef));
    }

    public Optional<RecordRef> getActiveSubRecord(ResourceId fieldId) {
        return Optional.ofNullable(activeSubRecords.get(fieldId));
    }

    public boolean isDeleted(RecordRef ref) {
        return deletedSubRecords.contains(ref);
    }
}
