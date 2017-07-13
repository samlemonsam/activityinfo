package org.activityinfo.ui.client.input.viewModel;


import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.type.RecordRef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * View model for a SubForm field.
 */
public class SubFormInputViewModel {

    private ResourceId fieldId;
    private List<SubRecordViewModel> subRecords;
    private Set<RecordRef> subRecordRefs = new HashSet<>();
    private boolean valid;

    SubFormInputViewModel(ResourceId fieldId, List<SubRecordViewModel> subRecords) {
        this.fieldId = fieldId;
        this.subRecords = subRecords;
        this.valid = true;
        for (SubRecordViewModel subRecord : subRecords) {
            if(!subRecord.isPlaceholder() && !subRecord.getSubFormViewModel().isValid()) {
                valid = false;
            }
            subRecordRefs.add(subRecord.getRecordRef());
        }
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public List<SubRecordViewModel> getSubRecords() {
        return subRecords;
    }

    public Set<RecordRef> getSubRecordRefs() {
        return subRecordRefs;
    }

    /**
     * Returns the placeholder {@link SubRecordViewModel} for this sub form field
     * if one exists.
     */
    public Optional<SubRecordViewModel> getPlaceholder() {
        for (SubRecordViewModel subRecord : subRecords) {
            if(subRecord.isPlaceholder()) {
                return Optional.of(subRecord);
            }
        }
        return Optional.absent();
    }

    public List<RecordUpdate> buildUpdates(RecordRef parentRef) {
        List<RecordUpdate> updates = new ArrayList<>();
        for (SubRecordViewModel subRecord : subRecords) {
            if(!subRecord.isPlaceholder()) {
                updates.add(subRecord.buildUpdate(parentRef));
            }
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
}
