package org.activityinfo.analysis.table;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.query.shared.FormSource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model's the user's selection of columns
 */
public class TableViewModel {

    private final FormSource formStore;
    private ResourceId formId;
    private Observable<FormTree> formTree;
    private TableModel tableModel;
    private Observable<EffectiveTableModel> effectiveTable;

    private Map<ResourceId, Observable<EffectiveTableModel>> effectiveSubTables = new HashMap<>();

    private StatefulValue<Optional<RecordRef>> selectedRecordRef = new StatefulValue<>(Optional.absent());
    private final Observable<Optional<SelectionViewModel>> selectionViewModel;

    public TableViewModel(final FormSource formStore, final TableModel tableModel) {
        this.formId = tableModel.getFormId();
        this.formStore = formStore;
        this.formTree = formStore.getFormTree(formId);
        this.tableModel = tableModel;
        this.effectiveTable = formTree.transform(tree -> new EffectiveTableModel(formStore, tree, tableModel));
        this.selectionViewModel = SelectionViewModel.compute(formStore, selectedRecordRef);
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public Observable<Optional<RecordRef>> getSelectedRecordRef() {
        // Don't actually expose the internal selection state ...
        // the *effective* selection is a product of our model state and the record status (deleted or not)
        return getSelectionViewModel().transform(record -> {
            if(record.isPresent()) {
                return Optional.of(record.get().getRef());
            } else {
                return Optional.absent();
            }
        });
    }

    public Observable<Optional<SelectionViewModel>> getSelectionViewModel() {
        return selectionViewModel;
    }

    public Observable<Optional<RecordTree>> getSelectedRecordTree() {
        return getSelectionViewModel().join(new Function<Optional<SelectionViewModel>, Observable<Optional<RecordTree>>>() {
            @Override
            public Observable<Optional<RecordTree>> apply(@Nullable Optional<SelectionViewModel> selection) {
                if(selection.isPresent()) {
                    return formStore.getRecordTree(selection.get().getRef()).transform(tree -> tree.getIfVisible());
                } else {
                    return Observable.just(Optional.absent());
                }
            }
        });
    }

    public Observable<EffectiveTableModel> getEffectiveTable() {
        return effectiveTable;
    }

    public Observable<EffectiveTableModel> getEffectiveSubTable(final ResourceId subFormId) {
        Observable<EffectiveTableModel> effectiveSubTable = effectiveSubTables.get(subFormId);
        if(effectiveSubTable == null) {
            final TableModel subModel = ImmutableTableModel.builder()
                    .formId(subFormId)
                    .build();

            effectiveSubTable = formTree
                    .transform(tree -> tree.subTree(subFormId))
                    .transform(subTree -> new EffectiveTableModel(formStore, subTree, subModel, Optional.of(getSelectedRecordRef())));

            effectiveSubTables.put(subFormId, effectiveSubTable);
        }
        return effectiveSubTable;
    }


    public ResourceId getFormId() {
        return formId;
    }

    public Observable<FormTree> getFormTree() {
        return formTree;
    }

    public void select(RecordRef ref) {
        selectedRecordRef.updateIfNotEqual(Optional.of(ref));
    }

}
