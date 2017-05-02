package org.activityinfo.ui.client.input.viewModel;


import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public class SubFormFieldViewModel {

    private ResourceId fieldId;
    private List<SubRecordViewModel> subRecords;


    SubFormFieldViewModel(ResourceId fieldId, List<SubRecordViewModel> subRecords) {
        this.fieldId = fieldId;
        this.subRecords = subRecords;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public List<SubRecordViewModel> getSubRecords() {
        return subRecords;
    }
}
