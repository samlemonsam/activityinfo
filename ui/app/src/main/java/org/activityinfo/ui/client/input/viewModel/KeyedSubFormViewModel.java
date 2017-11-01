package org.activityinfo.ui.client.input.viewModel;


import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.model.type.time.PeriodValue;

public class KeyedSubFormViewModel {
    private ResourceId fieldId;
    private SubRecordViewModel subRecord;
    private PeriodType periodType;

    KeyedSubFormViewModel(ResourceId fieldId, SubRecordViewModel subRecord, PeriodType periodType) {
        this.fieldId = fieldId;
        this.subRecord = subRecord;
        this.periodType = periodType;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public SubRecordViewModel getSubRecord() {
        return subRecord;
    }

    public RecordRef getActiveRecordRef() {
        return subRecord.getRecordRef();
    }

    public PeriodValue getActivePeriod() {
        return periodType.fromSubFormKey(getActiveRecordRef());
    }

    public boolean isValid() {
        return subRecord.getSubFormViewModel().isValid();
    }

    public RecordUpdate buildUpdates(RecordRef parentRecordRef) {
        return subRecord.buildUpdate(parentRecordRef);
    }
}
