package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.*;
import org.activityinfo.promise.Maybe;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.store.FormStore;

import java.util.Optional;

public class KeyedSubFormViewModelBuilder {
    private final ResourceId fieldId;
    private final ResourceId subFormId;
    private final FormTree subTree;
    private final SubFormKind subFormKind;
    private final PeriodType periodType;

    private final FormInputViewModelBuilder formBuilder;

    private final ActivePeriodMemory memory = new SimpleActivePeriodMemory();

    KeyedSubFormViewModelBuilder(FormStore formStore,
                                 FormTree parentTree,
                                 FormTree.Node node) {
        this.fieldId = node.getFieldId();
        this.subFormId = ((SubFormReferenceType) node.getType()).getClassId();
        this.subTree = parentTree.subTree(subFormId);
        this.subFormKind = subTree.getRootFormClass().getSubFormKind();
        this.formBuilder = new FormInputViewModelBuilder(formStore, subTree);
        this.periodType = periodTypeOf(subFormKind);
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public KeyedSubFormViewModel build(FormInputModel inputModel, Maybe<RecordTree> existingRecord) {

        // First find what the active record is.
        // We only show one record at a time
        RecordRef activeRef = computeActiveSubRecord(inputModel.getRecordRef(), inputModel);

        // Is there an existing record matching this ref?
        Maybe<RecordTree> existingSubRecordTree = Maybe.notFound();
        if(existingRecord.isVisible()) {
            if(existingRecord.get().contains(activeRef)) {
                existingSubRecordTree = Maybe.of(existingRecord.get().subTree(activeRef));
            }
        }

        // Is there any input from the user already?
        FormInputModel subInput = inputModel.getSubRecord(activeRef).orElse(new FormInputModel(activeRef));

        FormInputViewModel subInputViewModel = formBuilder.build(subInput, existingSubRecordTree);

        return new KeyedSubFormViewModel(fieldId, periodType, subInputViewModel);
    }

    private RecordRef computeActiveSubRecord(RecordRef parentRecordRef, FormInputModel inputModel) {
        // Has the user chosen a specific period?
        Optional<RecordRef> activeSubRecord = inputModel.getActiveSubRecord(fieldId);
        if(activeSubRecord.isPresent()) {
            return activeSubRecord.get();
        }

        // Otherwise choose the active record based on the user's previous choices
        // or the current date

        PeriodValue activePeriod = periodType.containingDate(memory.getLastUsedDate());
        ResourceId recordId = ResourceId.periodSubRecordId(parentRecordRef, activePeriod);

        return new RecordRef(subFormId, recordId);
    }


    private static PeriodType periodTypeOf(SubFormKind subFormKind) {
        switch (subFormKind) {
            case MONTHLY:
                return MonthType.INSTANCE;
            case WEEKLY:
                return EpiWeekType.INSTANCE;
            case DAILY:
                return LocalDateType.INSTANCE;
        }
        throw new IllegalArgumentException("kind:" + subFormKind);
    }
}
