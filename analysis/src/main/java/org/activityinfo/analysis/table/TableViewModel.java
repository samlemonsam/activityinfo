package org.activityinfo.analysis.table;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.query.shared.FormSource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Model's the user's selection of columns
 */
public class TableViewModel implements TableUpdater {

    private static final Logger LOGGER = Logger.getLogger(TableViewModel.class.getName());

    private final FormSource formStore;
    private ResourceId formId;
    private Observable<FormTree> formTree;
    private StatefulValue<TableModel> tableModel;
    private Observable<EffectiveTableModel> effectiveTable;
    private Observable<ColumnSet> columnSet;


    private Map<ResourceId, Observable<EffectiveTableModel>> effectiveSubTables = new HashMap<>();

    private StatefulValue<Optional<RecordRef>> selectedRecordRef = new StatefulValue<>(Optional.absent());
    private final Observable<Optional<SelectionViewModel>> selectionViewModel;

    public TableViewModel(final FormSource formStore, final TableModel tableModel) {
        this.formId = tableModel.getFormId();
        this.formStore = formStore;
        this.formTree = formStore.getFormTree(formId);
        this.tableModel = new StatefulValue<>(tableModel);
        this.effectiveTable = this.tableModel.join(tm -> {
            return formTree.transform(tree -> new EffectiveTableModel(formStore, tree, tm));
        });

        this.selectionViewModel = SelectionViewModel.compute(formStore, selectedRecordRef);
        this.columnSet = this.effectiveTable.join(table -> table.getColumnSet());
    }

    public Observable<TableModel> getTableModel() {
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

    public Observable<ColumnSet> getColumnSet() {
        return columnSet;
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

    public void update(TableModel updatedModel) {

        LOGGER.info("TableModel updated: " + updatedModel.toJson().toJson());

        tableModel.updateIfNotEqual(updatedModel);
    }

    @Override
    public void updateFilter(Optional<ExprNode> filterNode) {

        Optional<String> filter = filterNode.transform(n -> n.asExpression());

        tableModel.updateIfNotEqual(
            ImmutableTableModel.builder()
            .from(tableModel.get())
            .filter(filter)
            .build());

    }

    @Override
    public void updateColumnWidth(String columnId, int newWidth) {

        TableModel model = this.tableModel.get();

        List<TableColumn> updatedColumns = new ArrayList<>();
        for (TableColumn column : model.getColumns()) {
            if(column.getId().equals(columnId)) {
                updatedColumns.add(ImmutableTableColumn.builder().from(column).width(newWidth).build());
            } else {
                updatedColumns.add(column);
            }
        }

        tableModel.updateIfNotSame(ImmutableTableModel.builder()
                .from(model)
                .columns(updatedColumns)
                .build());
    }
}
