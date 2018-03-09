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
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.model.type.time.PeriodValue;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * View model for a SubForm field.
 */
public class SubFormViewModel {

    private ResourceId fieldId;
    private SubFormKind subFormKind;
    private List<FormInputViewModel> subRecords;
    private FormInputViewModel activeSubRecord;
    private Set<RecordRef> deletedRecords;
    private Set<RecordRef> subRecordRefs = new HashSet<>();
    private boolean valid;

    SubFormViewModel(ResourceId fieldId, List<FormInputViewModel> subRecords, Set<RecordRef> deletedRecords) {
        this(fieldId, SubFormKind.REPEATING, subRecords, null, deletedRecords);
    }

    SubFormViewModel(ResourceId fieldId,
                     SubFormKind subFormKind,
                     List<FormInputViewModel> subRecords,
                     FormInputViewModel activeSubRecord,
                     Set<RecordRef> deletedRecords) {
        this.fieldId = fieldId;
        this.subFormKind = subFormKind;
        this.subRecords = subRecords;
        this.activeSubRecord = activeSubRecord;
        this.deletedRecords = deletedRecords;
        this.valid = true;
        for (FormInputViewModel subRecord : subRecords) {
            if(!subRecord.isPlaceholder() && !subRecord.isValid()) {
                valid = false;
            }
            subRecordRefs.add(subRecord.getRecordRef());
        }
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public SubFormKind getSubFormKind() {
        return subFormKind;
    }

    public List<FormInputViewModel> getSubRecords() {
        return subRecords;
    }

    public Set<RecordRef> getSubRecordRefs() {
        return subRecordRefs;
    }

    /**
     * Returns the placeholder {@link FormInputViewModel} for this sub form field
     * if one exists.
     */
    public Optional<FormInputViewModel> getPlaceholder() {
        for (FormInputViewModel subRecord : subRecords) {
            if(subRecord.isPlaceholder()) {
                return Optional.of(subRecord);
            }
        }
        return Optional.absent();
    }

    public List<RecordUpdate> buildUpdates(RecordRef parentRef) {
        List<RecordUpdate> updates = new ArrayList<>();
        for (FormInputViewModel subRecord : subRecords) {
            if(!subRecord.isPlaceholder()) {
                updates.add(subRecord.buildUpdate(Optional.of(parentRef)));
            }
        }
        for (RecordRef deletedRecord : deletedRecords) {
            RecordUpdate update = new RecordUpdate();
            update.setFormId(deletedRecord.getFormId());
            update.setRecordId(deletedRecord.getRecordId());
            update.setDeleted(true);
            updates.add(update);
        }
        return updates;
    }

    /**
     *
     * @return true if all non-placeholder subforms are valid.
     */
    public boolean isValid() {
        return valid;
    }

    public RecordRef getActiveRecordRef() {
        assert activeSubRecord != null : "this subformkind does not have an active record";
        return activeSubRecord.getRecordRef();
    }

    public FormInputViewModel getActiveSubViewModel() {
        return this.activeSubRecord;
    }


    public PeriodValue getActivePeriod() {
        RecordRef ref = getActiveRecordRef();
        PeriodType periodType = subFormKind.getPeriodType();
        return periodType.fromSubFormKey(ref);
    }

    public Set<RecordRef> getDeletedRecords() {
        return deletedRecords;
    }

    public FormInputModel update(ResourceId fieldId, FieldInput input) {
        return getActiveSubViewModel().getInputModel().update(fieldId, input);
    }
}
