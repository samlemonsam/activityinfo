package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.type.RecordRef;

/**
 * View model for a single sub form record.
 */
public class SubRecordViewModel {

    private RecordRef recordRef;
    private FormInputViewModel subFormViewModel;
    private boolean placeholder;

    public SubRecordViewModel(RecordRef recordRef, FormInputViewModel subFormViewModel, boolean placeholder) {
        this.recordRef = recordRef;
        this.subFormViewModel = subFormViewModel;
        this.placeholder = placeholder;
    }

    /**
     * @return this sub form record's id
     */
    public RecordRef getRecordRef() {
        return recordRef;
    }

    public FormInputViewModel getSubFormViewModel() {
        return subFormViewModel;
    }

    /**
     *
     * @return true if this an empty sub record for a sub form with no entries.
     */
    public boolean isPlaceholder() {
        return placeholder;
    }

    public RecordUpdate buildUpdate() {
        return subFormViewModel.buildUpdate();
    }
}
