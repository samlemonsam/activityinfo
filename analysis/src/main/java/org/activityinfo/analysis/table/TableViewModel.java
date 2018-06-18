/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.analysis.table;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.analysis.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.SortModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.query.shared.FormSource;

import javax.annotation.Nullable;
import java.util.*;
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

    private Map<ResourceId, StatefulValue<TableModel>> subTableModels = new HashMap<>();
    private Map<ResourceId, Observable<EffectiveTableModel>> effectiveSubTables = new HashMap<>();

    private StatefulValue<Optional<RecordRef>> selectedRecordRef = new StatefulValue<>(Optional.absent());
    private final Observable<Optional<SelectionViewModel>> selectionViewModel;

    public TableViewModel(final FormSource formStore, final TableModel tableModel) {
        this.formId = tableModel.getFormId();
        this.formStore = formStore;
        this.formTree = formStore.getFormTree(formId);
        this.tableModel = new StatefulValue<>(tableModel);
        this.effectiveTable = computeEffectiveTableModel(this.tableModel);

        this.selectionViewModel = SelectionViewModel.compute(formStore, selectedRecordRef);
        this.columnSet = this.effectiveTable.join(table -> table.getColumnSet());
    }

    public Observable<EffectiveTableModel> computeEffectiveTableModel(Observable<TableModel> tableModel) {
        return tableModel.join(tm -> {
            return formTree.transform(tree -> new EffectiveTableModel(formStore, tree, tm));
        });
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

    public Observable<ApiViewModel> getApiViewModel() {
        return effectiveTable.transform(ApiViewModel::new);
    }

    public Observable<ColumnSet> getColumnSet() {
        return columnSet;
    }

    public Observable<EffectiveTableModel> getEffectiveSubTable(final ResourceId subFormId) {
        Observable<EffectiveTableModel> effectiveSubTable = effectiveSubTables.get(subFormId);
        if(effectiveSubTable == null) {
            StatefulValue<TableModel> subModel = new StatefulValue<>(ImmutableTableModel.builder()
                    .formId(subFormId)
                    .build());
            Observable<FormTree> subTree = formTree.transform(tree -> tree.subTree(subFormId));
            effectiveSubTable = Observable.transform(subTree, subModel, (tree, model) -> new EffectiveTableModel(formStore, tree, model, Optional.of(getSelectedRecordRef())));

            subTableModels.put(subFormId, subModel);
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
    public void updateFilter(Optional<FormulaNode> filterNode) {

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

    @Override
    public void updateSort(Optional<SortModel> sortModel) {
        TableModel model = this.tableModel.get();
        tableModel.updateIfNotSame(ImmutableTableModel
                .builder()
                .from(model)
                .sorting(sortModel.isPresent()
                        ? Collections.singleton(sortModel.get())
                        : Collections.EMPTY_LIST)
                .build());
    }

    public void updateSubFormSort(ResourceId subFormId, Optional<SortModel> sortModel) {
        StatefulValue<TableModel> subModel = subTableModels.get(subFormId);
        if (subModel == null) {
            throw new IllegalArgumentException(subFormId.asString());
        }
        subModel.updateIfNotSame(ImmutableTableModel.builder()
                .from(subModel.get())
                .sorting(sortModel.isPresent()
                        ? Collections.singleton(sortModel.get())
                        : Collections.EMPTY_LIST)
                .build());
    }

    public Observable<ExportViewModel> computeExportModel(
            Observable<ResourceId> selectedForm,
            Observable<ExportScope> columnScope) {
        return computeExportModel(selectedForm, columnScope, Observable.just(ExportScope.ALL));
    }

    public Observable<ExportViewModel> computeExportModel(
            Observable<ResourceId> selectedForm,
            Observable<ExportScope> columnScope,
            Observable<ExportScope> rowScope) {

        Observable<EffectiveTableModel> parentFormModel = getEffectiveTable();
        Observable<Optional<EffectiveTableModel>> subFormModel = selectedForm.join(formId -> {
            if(formId.equals(tableModel.get().getFormId())) {
                // Parent form has been selected, no sub form model
                return Observable.just(Optional.absent());
            } else {
                return getEffectiveSubTable(formId).transform(Optional::of);
            }
        });

        Observable<TableModel> exportTableModel = Observable.transform(parentFormModel, subFormModel, columnScope, rowScope, (parent, sub, columns, rows) -> {
            ImmutableTableModel.Builder model = ImmutableTableModel.builder();
            if(sub.isPresent()) {
                model.formId(sub.get().getFormId());
                if(columns == ExportScope.SELECTED ) {
                    for (EffectiveTableColumn tableColumn : parent.getColumns()) {
                        model.addColumns(ImmutableTableColumn.builder()
                                .label(tableColumn.getLabel())
                                .formula(parentFormula(tableColumn.getFormula()))
                                .build());

                    }
                    for (EffectiveTableColumn tableColumn : sub.get().getColumns()) {
                        model.addColumns(ImmutableTableColumn.builder()
                                .label(tableColumn.getLabel())
                                .formula(tableColumn.getFormulaString())
                                .build());
                    }
                }
                if (rows == ExportScope.SELECTED) {
                    model.filter(parent.getFilter());
                }
            } else {
                model.formId(parent.getFormId());
                if(columns == ExportScope.SELECTED) {
                    for (EffectiveTableColumn tableColumn : parent.getColumns()) {
                        model.addColumns(ImmutableTableColumn.builder()
                                .label(tableColumn.getLabel())
                                .formula(tableColumn.getFormulaString())
                                .build());

                    }
                }
                if (rows == ExportScope.SELECTED) {
                    model.filter(parent.getFilter());
                }
            }
            return model.build();
        });
        Observable<Boolean> colLimitExceed = computeEffectiveTableModel(exportTableModel).transform(ExportViewModel::columnLimitExceeded);
        return Observable.transform(exportTableModel, colLimitExceed, ExportViewModel::new);
    }

    private String parentFormula(ParsedFormula formula) {

        if(!formula.isValid()) {
            return formula.getFormula();
        }

        SymbolNode parentSymbol = new SymbolNode(ColumnModel.PARENT_SYMBOL);
        FormulaNode transformed = formula.getRootNode().transform(node -> {
           if(node instanceof SymbolNode) {
               // A -> parent.A
               return new CompoundExpr(parentSymbol, (SymbolNode) node);
           } else {
               return node;
           }
        });
        return transformed.asExpression();
    }
}
