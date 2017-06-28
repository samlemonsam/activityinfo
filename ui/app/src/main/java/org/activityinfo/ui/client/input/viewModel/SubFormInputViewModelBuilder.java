package org.activityinfo.ui.client.input.viewModel;


import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class which constructs a {@link SubFormInputViewModel}.
 *
 * <p>This class is built from a sub form's form tree and includes all the pre-computed information
 * need to quickly compute a {@link SubFormInputViewModel} from a {@link FormInput}.
 */
class SubFormInputViewModelBuilder {

    private final ResourceId fieldId;
    private final ResourceId subFormId;
    private final FormInputViewModelBuilder formBuilder;

    private ResourceId placeholderRecordId;

    SubFormInputViewModelBuilder(FormStore formStore, FormTree.Node node, FormTree subTree) {
        this.fieldId = node.getFieldId();
        this.subFormId = subTree.getRootFormId();
        this.formBuilder = new FormInputViewModelBuilder(formStore, subTree);
        this.placeholderRecordId = ResourceId.generateId();
    }

    public SubFormInputViewModel build(FormInputModel inputModel) {
        List<SubRecordViewModel> subRecords = new ArrayList<>();

        for (FormInputModel model : inputModel.getSubRecords()) {
            if(model.getRecordRef().getFormId().equals(subFormId)) {
                subRecords.add(new SubRecordViewModel(model.getRecordRef(), formBuilder.build(model), false));
            }
        }

        // If there are no records, then the computed view includes a new empty one
        if(subRecords.isEmpty()) {
            RecordRef newRecordRef = placeholderRecordRef();
            subRecords.add(new SubRecordViewModel(newRecordRef, formBuilder.build(new FormInputModel(newRecordRef)), true));
        }

        return new SubFormInputViewModel(fieldId, subRecords);
    }

    private RecordRef placeholderRecordRef() {
        return new RecordRef(subFormId, placeholderRecordId);
    }

    public ResourceId getFieldId() {
        return fieldId;
    }
}