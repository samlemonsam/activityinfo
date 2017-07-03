package org.activityinfo.ui.client.input.viewModel;


import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.promise.Maybe;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.store.FormStore;

import java.util.*;

/**
 * Helper class which constructs a {@link SubFormInputViewModel}.
 *
 * <p>This class is built from a sub form's form tree and includes all the pre-computed information
 * need to quickly compute a {@link SubFormInputViewModel} from a {@link FormInputModel}.
 */
class SubFormInputViewModelBuilder {

    private final ResourceId fieldId;
    private final ResourceId subFormId;
    private FormTree subTree;
    private final FormInputViewModelBuilder formBuilder;

    private ResourceId placeholderRecordId;

    SubFormInputViewModelBuilder(FormStore formStore,
                                 FormTree parentTree,
                                 FormTree.Node node) {
        this.fieldId = node.getFieldId();
        this.subFormId = ((SubFormReferenceType) node.getType()).getClassId();
        this.subTree = parentTree.subTree(subFormId);
        this.placeholderRecordId = ResourceId.generateId();
        this.formBuilder = new FormInputViewModelBuilder(formStore, subTree);
    }

    public SubFormInputViewModel build(FormInputModel inputModel, Maybe<RecordTree> existingParentRecord) {

        List<SubRecordViewModel> subRecordViews = new ArrayList<>();

        // First do existing records

        Set<RecordRef> existingRefs = new HashSet<>();
        List<RecordTree> existingSubTrees = existingParentRecord
            .transform(parentTree -> parentTree.buildSubTrees(inputModel.getRecordRef(), subTree))
            .or(Collections.emptyList());

        for (RecordTree existingSubRecord : existingSubTrees) {
            RecordRef ref = existingSubRecord.getRootRef();

            FormInputModel subInput = inputModel.getSubRecord(ref).orElse(new FormInputModel(ref));
            FormInputViewModel subViewModel = formBuilder.build(subInput, Maybe.of(existingSubRecord));

            existingRefs.add(ref);
            subRecordViews.add(new SubRecordViewModel(ref, subViewModel, false));
        }

        // Now add sub records newly added by the user
        for (FormInputModel subInput : inputModel.getSubRecords()) {
            if(!existingRefs.contains(subInput.getRecordRef())) {
                if (subInput.getRecordRef().getFormId().equals(subFormId)) {
                    FormInputViewModel subViewModel = formBuilder.build(subInput, Maybe.notFound());
                    subRecordViews.add(new SubRecordViewModel(subInput.getRecordRef(), subViewModel, false));
                }
            }
        }

        // If there are no records, then the computed view includes a new empty one
        if(subRecordViews.isEmpty()) {
            RecordRef newRecordRef = placeholderRecordRef();
            FormInputViewModel subViewModel = formBuilder.build(new FormInputModel(newRecordRef), Maybe.notFound());
            subRecordViews.add(new SubRecordViewModel(newRecordRef, subViewModel, true));
        }

        return new SubFormInputViewModel(fieldId, subRecordViews);
    }

    private RecordRef placeholderRecordRef() {
        return new RecordRef(subFormId, placeholderRecordId);
    }

    public ResourceId getFieldId() {
        return fieldId;
    }
}