package org.activityinfo.analysis.table;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.analysis.FormSource;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;

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
    private Observable<EffectiveTableModel> effectiveTable;

    private Map<ResourceId, Observable<EffectiveTableModel>> effectiveSubTables = new HashMap<>();

    private StatefulValue<Optional<RecordRef>> selectedRecordRef = new StatefulValue<>(Optional.<RecordRef>absent());
    private final Observable<Optional<FormRecord>> selectedRecord;

    public TableViewModel(final FormSource formStore, final TableModel tableModel) {
        this.formId = tableModel.getFormId();
        this.formStore = formStore;
        this.formTree = formStore.getFormTree(formId);
        this.effectiveTable = formTree.transform(new Function<FormTree, EffectiveTableModel>() {
            @Override
            public EffectiveTableModel apply(FormTree formTree) {
                return new EffectiveTableModel(formStore, formTree, tableModel);
            }
        });
        this.selectedRecord = selectedRecordRef.join(new Function<Optional<RecordRef>, Observable<Optional<FormRecord>>>() {
            @Override
            public Observable<Optional<FormRecord>> apply(Optional<RecordRef> selection) {
                if (!selection.isPresent()) {
                    return Observable.just(Optional.<FormRecord>absent());
                }
                return formStore.getRecord(selection.get()).transform(new Function<FormRecord, Optional<FormRecord>>() {
                    @Nullable
                    @Override
                    public Optional<FormRecord> apply(@Nullable FormRecord reference) {
                        return Optional.of(reference);
                    }
                });
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

    public Observable<EffectiveTableModel> getEffectiveSubTable(final ResourceId subFormId) {
        Observable<EffectiveTableModel> effectiveSubTable = effectiveSubTables.get(subFormId);
        if(effectiveSubTable == null) {
            final TableModel subModel = ImmutableTableModel.builder()
                    .formId(subFormId)
                    .build();

            effectiveSubTable = formTree
                    .transform(new Function<FormTree, FormTree>() {
                        @Override
                        public FormTree apply(FormTree tree) {
                            return tree.subTree(subFormId);
                        }
                    })
                    .transform(new Function<FormTree, EffectiveTableModel>() {
                        @Override
                        public EffectiveTableModel apply(FormTree subTree) {
                            return new EffectiveTableModel(formStore, subTree, subModel);
                        }
                    });

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
