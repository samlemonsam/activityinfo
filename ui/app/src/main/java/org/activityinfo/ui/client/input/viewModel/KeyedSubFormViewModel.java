package org.activityinfo.ui.client.input.viewModel;


import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;

public class KeyedSubFormViewModel {
    private ResourceId fieldId;
    private SubRecordViewModel subRecord;

    KeyedSubFormViewModel(ResourceId fieldId, SubRecordViewModel subRecord) {
        this.fieldId = fieldId;
        this.subRecord = subRecord;
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

    public boolean isValid() {
        return subRecord.getSubFormViewModel().isValid();
    }

    public RecordUpdate buildUpdates(RecordRef parentRecordRef) {
        return subRecord.buildUpdate(parentRecordRef);
    }
}
