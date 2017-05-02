package org.activityinfo.ui.client.input.viewModel;


import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubFormInputViewModel {

    private ResourceId fieldId;
    private List<SubRecordViewModel> subRecords;
    private Set<RecordRef> subRecordRefs = new HashSet<>();

    SubFormInputViewModel(ResourceId fieldId, List<SubRecordViewModel> subRecords) {
        this.fieldId = fieldId;
        this.subRecords = subRecords;
        for (SubRecordViewModel subRecord : subRecords) {
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

    public Optional<SubRecordViewModel> getPlaceholder() {
        for (SubRecordViewModel subRecord : subRecords) {
            if(subRecord.isPlaceholder()) {
                return Optional.of(subRecord);
            }
        }
        return Optional.absent();
    }

}
