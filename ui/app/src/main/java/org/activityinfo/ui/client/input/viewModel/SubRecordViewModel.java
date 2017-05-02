package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.resource.ResourceId;

public class SubRecordViewModel {

    private ResourceId recordId;
    private FormInputViewModel subFormViewModel;

    public SubRecordViewModel(ResourceId recordId, FormInputViewModel subFormViewModel) {
        this.recordId = recordId;
        this.subFormViewModel = subFormViewModel;
    }

}
