package org.activityinfo.ui.client.input.viewModel;


import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.PeriodValue;
import org.activityinfo.promise.Maybe;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.store.FormStore;

import java.util.*;

/**
 * Helper class which constructs a {@link SubFormViewModel}.
 *
 * <p>This class is built from a sub form's form tree and includes all the pre-computed information
 * need to quickly compute a {@link SubFormViewModel} from a {@link FormInputModel}.
 */
class SubFormViewModelBuilder {

    private final ResourceId fieldId;
    private final ResourceId subFormId;
    private final FormTree subTree;
    private final SubFormKind subFormKind;
    private final FormInputViewModelBuilder formBuilder;
    private final ActivePeriodMemory memory = new SimpleActivePeriodMemory();

    private ResourceId placeholderRecordId;

    SubFormViewModelBuilder(FormStore formStore,
                            FormTree parentTree,
                            FormTree.Node node) {
        this.fieldId = node.getFieldId();
        this.subFormId = ((SubFormReferenceType) node.getType()).getClassId();
        this.subTree = parentTree.subTree(subFormId);
        this.subFormKind = subTree.getRootFormClass().getSubFormKind();
        this.placeholderRecordId = ResourceId.generateId();
        this.formBuilder = new FormInputViewModelBuilder(formStore, subTree);
    }

    public SubFormViewModel build(FormInputModel inputModel, Maybe<RecordTree> existingParentRecord) {

        List<FormInputViewModel> subRecordViews = new ArrayList<>();

        // First do existing records

        Set<RecordRef> existingRefs = new HashSet<>();
        List<RecordTree> existingSubTrees;
        if(existingParentRecord.isVisible()) {
            existingSubTrees = existingParentRecord.get().buildSubTrees(inputModel.getRecordRef(), subTree);
        } else {
            existingSubTrees = Collections.emptyList();
        }

        for (RecordTree existingSubRecord : existingSubTrees) {
            RecordRef ref = existingSubRecord.getRootRef();

            FormInputModel subInput = inputModel.getSubRecord(ref).orElse(new FormInputModel(ref));
            FormInputViewModel subViewModel = formBuilder.build(subInput, Maybe.of(existingSubRecord));

            existingRefs.add(ref);
            subRecordViews.add(subViewModel);
        }

        // Now add sub records newly added by the user
        for (FormInputModel subInput : inputModel.getSubRecords()) {
            if(!existingRefs.contains(subInput.getRecordRef())) {
                if (subInput.getRecordRef().getFormId().equals(subFormId)) {
                    FormInputViewModel subViewModel = formBuilder.build(subInput, Maybe.notFound());
                    subRecordViews.add(subViewModel);
                }
            }
        }

        if(subFormKind == SubFormKind.REPEATING) {

            // If there are no records, then the computed view includes a new empty one

            if(subRecordViews.isEmpty()) {
                FormInputViewModel subViewModel = formBuilder.placeholder(placeholderRecordRef());
                subRecordViews.add(subViewModel);
            }

            return new SubFormViewModel(fieldId, subRecordViews);


        } else {

            // Keyed/Period subforms have a single active record

            RecordRef activeRecord = computeActiveSubRecord(inputModel.getRecordRef(), inputModel);
            FormInputViewModel activeRecordViewModel = find(activeRecord, subRecordViews);

            return new SubFormViewModel(fieldId, subFormKind, subRecordViews, activeRecordViewModel);
        }
    }

    private FormInputViewModel find(RecordRef activeRecord, List<FormInputViewModel> subRecordViews) {
        for (FormInputViewModel subRecordView : subRecordViews) {
            if(subRecordView.getRecordRef().equals(activeRecord)) {
                return subRecordView;
            }
        }
        return formBuilder.placeholder(activeRecord);
    }


    private RecordRef computeActiveSubRecord(RecordRef parentRecordRef, FormInputModel inputModel) {
        // Has the user chosen a specific period?
        Optional<RecordRef> activeSubRecord = inputModel.getActiveSubRecord(fieldId);
        if(activeSubRecord.isPresent()) {
            return activeSubRecord.get();
        }

        // Otherwise choose the active record based on the user's previous choices
        // or the current date

        PeriodValue activePeriod = subFormKind.getPeriodType().containingDate(memory.getLastUsedDate());
        ResourceId recordId = ResourceId.periodSubRecordId(parentRecordRef, activePeriod);

        return new RecordRef(subFormId, recordId);
    }


    private RecordRef placeholderRecordRef() {
        return new RecordRef(subFormId, placeholderRecordId);
    }

    public ResourceId getFieldId() {
        return fieldId;
    }
}