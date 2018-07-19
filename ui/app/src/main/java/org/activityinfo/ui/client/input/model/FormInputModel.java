/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    private final Set<ResourceId> touchedFields;
    private final boolean validationRequested;
    private final Map<RecordRef, FormInputModel> subRecords;
    private final Map<ResourceId, RecordRef> activeSubRecords;
    private final Set<RecordRef> deletedSubRecords;

    public FormInputModel(RecordRef recordRef) {
        this.recordRef = recordRef;
        fieldInputs = Collections.emptyMap();
        subRecords = Collections.emptyMap();
        activeSubRecords = Collections.emptyMap();
        deletedSubRecords = Collections.emptySet();
        touchedFields = Collections.emptySet();
        validationRequested = false;
    }

    private FormInputModel(RecordRef recordRef,
                           Map<ResourceId, FieldInput> fieldInputs,
                           Map<RecordRef, FormInputModel> subRecords,
                           Map<ResourceId, RecordRef> activeSubRecords,
                           Set<RecordRef> deletedSubRecords,
                           Set<ResourceId> touchedFields,
                           boolean validationRequested) {
        this.recordRef = recordRef;
        this.fieldInputs = fieldInputs;
        this.touchedFields = touchedFields;
        this.validationRequested = validationRequested;
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

        return new FormInputModel(
                recordRef,
                fieldInputs,
                subRecords,
                updatedMap,
                deletedSubRecords,
                touchedFields,
                validationRequested);
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
        Set<ResourceId> updatedTouchSet = add(this.touchedFields, fieldId);

        return new FormInputModel(this.recordRef,
                updatedInputs,
                updatedSubRecords,
                activeSubRecords,
                deletedSubRecords,
                updatedTouchSet,
                validationRequested);
    }

    public FormInputModel updateSubForm(FormInputModel subFormInputModel) {
        assert !subFormInputModel.getRecordRef().getFormId().equals(this.recordRef.getFormId()) :
            "should only be used for sub input models, not the root input model";

        Map<RecordRef, FormInputModel> updatedSubRecords = new HashMap<>(this.subRecords);
        updatedSubRecords.put(subFormInputModel.getRecordRef(), subFormInputModel);

        return new FormInputModel(this.recordRef,
            this.fieldInputs,
            updatedSubRecords,
            activeSubRecords,
            deletedSubRecords,
            touchedFields,
            validationRequested);
    }

    public FormInputModel touch(RecordRef recordRef, ResourceId fieldId) {
        if(this.recordRef.equals(recordRef)) {
            return touch(fieldId);
        }

        HashMap<RecordRef, FormInputModel> updatedSubRecords = new HashMap<>();
        for (Map.Entry<RecordRef, FormInputModel> entry : subRecords.entrySet()) {
            if(entry.getKey().equals(recordRef)) {
                updatedSubRecords.put(entry.getKey(), entry.getValue().touch(fieldId));
            } else {
                updatedSubRecords.put(entry.getKey(), entry.getValue());
            }
        }

        return new FormInputModel(this.recordRef,
                this.fieldInputs,
                updatedSubRecords,
                activeSubRecords,
                deletedSubRecords,
                touchedFields,
                validationRequested);
    }

    public FormInputModel touch(ResourceId fieldId) {
        if(this.touchedFields.contains(fieldId)) {
            return this;
        } else {
            return new FormInputModel(
                    recordRef,
                    fieldInputs,
                    subRecords,
                    activeSubRecords,
                    deletedSubRecords,
                    add(touchedFields, fieldId),
                    validationRequested);
        }
    }


    public FormInputModel validationRequested() {
        return validationRequested(getRecordRef());
    }

    public FormInputModel validationRequested(RecordRef ref) {

        boolean validationRequested = this.validationRequested;

        if(ref.equals(recordRef) && !validationRequested) {
            validationRequested = true;
        }

        Map<RecordRef, FormInputModel> updatedSubRecords = new HashMap<>();
        for (Map.Entry<RecordRef, FormInputModel> entry : subRecords.entrySet()) {
            if(ref.equals(recordRef) || entry.getValue().getRecordRef().equals(ref)) {
                updatedSubRecords.put(entry.getKey(), entry.getValue().validationRequested());
            }
        }

        return new FormInputModel(
                recordRef,
                fieldInputs,
                updatedSubRecords,
                activeSubRecords,
                deletedSubRecords,
                touchedFields,
                validationRequested);
    }

    /**
     * @return true if the user has explicitly requested validation for this form,
     * by example clicking the save button.
     */
    public boolean isValidationRequested() {
        return validationRequested;
    }

    /**
     * @return true if the user has "touched" the given field in any way, and so should,
     * for example, see a validation message of this field.
     */
    public boolean isTouched(ResourceId fieldId) {
        return touchedFields.contains(fieldId);
    }

    private static Set<ResourceId> add(Set<ResourceId> set, ResourceId fieldId) {
        if(set.contains(fieldId)) {
            return set;
        } else {
            Set<ResourceId> newSet = new HashSet<>(set);
            newSet.add(fieldId);
            return newSet;
        }
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
                newDeleted,
                touchedFields,
                validationRequested);
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
                deletedSubRecords,
                touchedFields,
                validationRequested);
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
