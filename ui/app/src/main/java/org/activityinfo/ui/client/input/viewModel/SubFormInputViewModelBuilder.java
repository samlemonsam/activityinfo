package org.activityinfo.ui.client.input.viewModel;


import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.input.model.FormInputModel;

import java.util.ArrayList;
import java.util.List;

public class SubFormInputViewModelBuilder {

    private ResourceId fieldId;
    private FormInputViewModelBuilder formBuilder;

    public SubFormInputViewModelBuilder(FormTree.Node node, FormTree subTree) {
        this.fieldId = node.getFieldId();
        this.formBuilder = new FormInputViewModelBuilder(subTree);
    }

    public SubFormFieldViewModel build() {
        List<SubRecordViewModel> subRecords = new ArrayList<>();

        // If there are no records, then the computed view includes a new empty one
        subRecords.add(new SubRecordViewModel(ResourceId.generateId(), formBuilder.build(new FormInputModel())));

        return new SubFormFieldViewModel(fieldId, subRecords);
    }

    public ResourceId getFieldId() {
        return fieldId;
    }
}