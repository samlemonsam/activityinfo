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

    FormTree formTree;
    FormInputModel inputModel;
    Map<ResourceId, FieldValue> existingValues;
    Map<ResourceId, FieldValue> fieldValueMap;
    Map<ResourceId, SubFormViewModel> subFormMap;
    Set<ResourceId> relevant;
    Set<ResourceId> missing;
    Multimap<ResourceId, String> validationErrors;
    boolean valid;
    boolean locked;
    boolean dirty;
    boolean placeholder;

    FormInputViewModel() {
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

    public boolean isMissingErrorVisible(ResourceId fieldId) {
        return missing.contains(fieldId) &&
                (inputModel.isValidationRequested() || inputModel.isTouched(fieldId));
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

    public boolean isLocked() {
        return locked;
    }
}
