package org.activityinfo.ui.client.input.view;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;

/**
 * Applies inputs from the view to the model.
 */
public interface InputHandler {

    void updateModel(RecordRef record, ResourceId fieldId, FieldInput value);

    void addSubRecord(RecordRef subRecordRef);

    void deleteSubRecord(RecordRef recordRef);

    void changeActiveSubRecord(ResourceId fieldId, RecordRef newActiveRef);

    void updateSubModel(FormInputModel update);
}
