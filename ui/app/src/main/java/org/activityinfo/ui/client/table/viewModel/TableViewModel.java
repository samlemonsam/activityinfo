package org.activityinfo.ui.client.table.viewModel;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.model.ImmutableTableModel;
import org.activityinfo.ui.client.table.model.TableModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Model's the user's selection of columns
 */
public class TableViewModel {

    private final FormStore formStore;
    private ResourceId formId;
    private Observable<FormTree> formTree;
    private Observable<EffectiveTableModel> effectiveTable;

    private Map<ResourceId, Observable<EffectiveTableModel>> effectiveSubTables = new HashMap<>();

    private StatefulValue<Optional<RecordRef>> selectedRecordRef = new StatefulValue<>(Optional.absent());
    private final Observable<Optional<FormRecord>> selectedRecord;

    public TableViewModel(final FormStore formStore, TableModel tableModel) {
        this.formId = tableModel.getFormId();
        this.formStore = formStore;
        this.formTree = formStore.getFormTree(formId);
        this.effectiveTable = formTree.transform(tree -> new EffectiveTableModel(formStore, tree, tableModel));
        this.selectedRecord = selectedRecordRef.join(new Function<Optional<RecordRef>, Observable<Optional<FormRecord>>>() {
            @Override
            public Observable<Optional<FormRecord>> apply(Optional<RecordRef> selection) {
                if (!selection.isPresent()) {
                    return Observable.just(Optional.absent());
                }
                return formStore.getRecord(selection.get()).transform(Optional::of);
            }
        });
    }

    public Observable<Optional<RecordRef>> getSelectedRecordRef() {
        return selectedRecordRef;
    }

    public Observable<Optional<FormRecord>> getSelectedRecord() {
        return selectedRecord;
    }

    public Observable<EffectiveTableModel> getEffectiveTable() {
        return effectiveTable;
    }

    public Observable<EffectiveTableModel> getEffectiveSubTable(ResourceId subFormId) {
        Observable<EffectiveTableModel> effectiveSubTable = effectiveSubTables.get(subFormId);
        if(effectiveSubTable == null) {
            TableModel subModel = ImmutableTableModel.builder()
                    .formId(subFormId)
                    .build();

            effectiveSubTable = formTree
                    .transform(tree -> tree.subTree(subFormId))
                    .transform(subTree -> new EffectiveTableModel(formStore, subTree, subModel));

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

    public FormStore getFormStore() {
        return formStore;
    }

    public void select(RecordRef ref) {
        selectedRecordRef.updateIfNotEqual(Optional.of(ref));
    }
}
