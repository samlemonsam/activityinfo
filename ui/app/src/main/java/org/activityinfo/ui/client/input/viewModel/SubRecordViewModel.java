package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.type.RecordRef;

public class SubRecordViewModel {

    private RecordRef recordRef;
    private FormInputViewModel subFormViewModel;
    private boolean placeholder;

    public SubRecordViewModel(RecordRef recordRef, FormInputViewModel subFormViewModel, boolean placeholder) {
        this.recordRef = recordRef;
        this.subFormViewModel = subFormViewModel;
        this.placeholder = placeholder;
    }

    public RecordRef getRecordRef() {
        return recordRef;
    }

    public FormInputViewModel getSubFormViewModel() {
        return subFormViewModel;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }
}
