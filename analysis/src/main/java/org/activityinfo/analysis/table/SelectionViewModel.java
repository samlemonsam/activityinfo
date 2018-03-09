package org.activityinfo.analysis.table;

import com.google.common.base.Optional;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.query.shared.FormSource;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Describes the selected record and the permissions that apply to the current user.
 */
public class SelectionViewModel {

    private static final Logger LOGGER = Logger.getLogger(SelectionViewModel.class.getName());

    private FormInstance record;
    private boolean editAllowed;
    private boolean deleteAllowed;

    private SelectionViewModel(FormInstance record, boolean editAllowed, boolean deleteAllowed) {
        this.record = record;
        this.editAllowed = editAllowed;
        this.deleteAllowed = deleteAllowed;
    }

    public RecordRef getRef() {
        return record.getRef();
    }

    public FormInstance getRecord() {
        return record;
    }

    public boolean isEditAllowed() {
        return editAllowed;
    }

    public boolean isDeleteAllowed() {
        return deleteAllowed;
    }


    public static Observable<Optional<SelectionViewModel>> compute(
        FormSource formSource,
        StatefulValue<Optional<RecordRef>> selectedRecordRef) {

        return selectedRecordRef.join(selection -> {
            if (!selection.isPresent()) {
                return Observable.just(Optional.absent());
            } else {
                return compute(formSource, selection.get());
            }
        });
    }


    private static Observable<Optional<SelectionViewModel>> compute(FormSource formSource, RecordRef ref) {
        Observable<FormTree> formTree = formSource.getFormTree(ref.getFormId());
        Observable<Maybe<FormRecord>> record = formSource.getRecord(ref);

        return Observable.transform(formTree, record, SelectionViewModel::computeSelection);
    }

    private static Optional<SelectionViewModel> computeSelection(FormTree formTree, Maybe<FormRecord> record) {
        if(!record.isVisible()) {
            return Optional.absent();
        }
        FormMetadata form = formTree.getFormMetadata(ResourceId.valueOf(record.get().getFormId()));
        if(!form.isVisible()) {
            return Optional.absent();
        }
        FormInstance typedRecord = FormInstance.toFormInstance(form.getSchema(), record.get());

        boolean editAllowed = evalPermission(form, typedRecord, FormOperation.EDIT_RECORD);

        return Optional.of(new SelectionViewModel(typedRecord, editAllowed, editAllowed));
    }

    private static boolean evalPermission(FormMetadata form, FormInstance record, FormOperation operation) {
        if(!form.getPermissions().isAllowed(operation)) {
            return false;
        }
        if(!form.getPermissions().isFiltered(operation)) {
            return true;
        }
        String filter = form.getPermissions().getFilter(operation);
        try {
            FormEvalContext context = new FormEvalContext(form.getSchema(), record);
            FormulaNode formula = FormulaParser.parse(filter);
            FieldValue result = formula.evaluate(context);

            return result == BooleanFieldValue.TRUE;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to evaluate permission filter '" + filter + "'", e);
            return false;
        }
    }

}
