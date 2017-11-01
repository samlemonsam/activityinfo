package org.activityinfo.ui.client.input.viewModel;


import com.google.common.base.Optional;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.model.type.time.PeriodValue;

public class KeyedSubFormViewModel {
    private ResourceId fieldId;
    private FormInputViewModel activeSubViewModel;
    private PeriodType periodType;

    KeyedSubFormViewModel(ResourceId fieldId, PeriodType periodType, FormInputViewModel activeSubViewModel) {
        this.fieldId = fieldId;
        this.periodType = periodType;
        this.activeSubViewModel = activeSubViewModel;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public FormInputViewModel getActiveSubViewModel() {
        return activeSubViewModel;
    }

    public RecordRef getActiveRecordRef() {
        return activeSubViewModel.getRecordRef();
    }

    public PeriodValue getActivePeriod() {
        return periodType.fromSubFormKey(getActiveRecordRef());
    }

    public boolean isValid() {
        return activeSubViewModel.isValid();
    }


    public boolean isDirty() {
        return activeSubViewModel.isDirty();
    }

    public RecordUpdate buildUpdates(RecordRef parentRecordRef) {
        return activeSubViewModel.buildUpdate(Optional.of(parentRecordRef));
    }
}
