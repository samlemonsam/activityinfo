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

import com.google.common.base.Optional;
import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.formula.ConstantNode;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.Formulas;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.EpiWeekType;
import org.activityinfo.model.type.time.FortnightType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.model.type.time.MonthType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.query.shared.FormSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The effective description of a table
 */
public class EffectiveTableModel {


    public static final String ID_COLUMN_ID = "$$id";

    private FormTree formTree;
    private TableModel tableModel;
    private Optional<Observable<Optional<RecordRef>>> selectedParentRef;
    private List<EffectiveTableColumn> columns;
    private Observable<ColumnSet> columnSet;

    public EffectiveTableModel(FormSource formSource, FormTree formTree, TableModel tableModel) {
        this(formSource, formTree, tableModel, Optional.absent());
    }

    public EffectiveTableModel(FormSource formSource, FormTree formTree, TableModel tableModel,
                               Optional<Observable<Optional<RecordRef>>> selectedParentRef) {
        this.formTree = formTree;
        this.tableModel = tableModel;
        this.selectedParentRef = selectedParentRef;
        this.columnSet = new StatefulValue<>();
        this.columns = new ArrayList<>();

        if(formTree.getRootState() == FormTree.State.VALID) {
            if (tableModel.getColumns().isEmpty()) {
                addDefaultColumns(formTree);
            } else {
                for (TableColumn tableColumn : tableModel.getColumns()) {
                    columns.add(new EffectiveTableColumn(formTree, tableColumn));
                }
            }
        }

        if(selectedParentRef.isPresent()) {
            // For sub form views, the columnset depends on the selection
            this.columnSet = selectedParentRef.get().join(parentRecord -> {
                if(parentRecord.isPresent()) {
                    return formSource.query(buildQuery(columns, parentRecord.get()));
                } else {
                    return Observable.just(emptyColumnSet(columns));
                }
            });
        } else {
            this.columnSet = formSource.query(buildQuery(columns));
        }
    }

    public TableModel getModel() {
        return tableModel;
    }

    public Optional<String> getFilter() {
        return getModel().getFilter();
    }


    private void addDefaultColumns(FormTree formTree) {
        if(!isSubTable() && formTree.getRootFormClass().isSubForm()) {
            addDefaultColumns(formTree.parentTree());
        }
        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.getField().isVisible()) {
                if (isSimple(node.getType())) {
                    columns.add(new EffectiveTableColumn(formTree, defaultColumnModel(node)));

                } else if (node.getType() instanceof ReferenceType && !node.isParentReference()) {
                    addKeyColumns(columns, node);
                }
            }
        }
    }

    public String getTitle() {
        return formTree.getRootFormClass().getLabel();
    }

    public FormTree.State getRootFormState() {
        return this.formTree.getRootState();
    }

    private boolean isSimple(FieldType type) {
        return type instanceof TextType ||
               type instanceof NarrativeType ||
               type instanceof SerialNumberType ||
               type instanceof QuantityType ||
               type instanceof BarcodeType ||
               type instanceof EnumType ||
               type instanceof GeoPointType ||
               type instanceof LocalDateType ||
               type instanceof EpiWeekType ||
               type instanceof FortnightType ||
               type instanceof MonthType ||
               type instanceof CalculatedFieldType;
    }

    private ImmutableTableColumn defaultColumnModel(FormTree.Node node) {
        return defaultColumnModel(node.getPath().toExpr());
    }

    private ImmutableTableColumn defaultColumnModel(FormulaNode formulaNode) {
        String formulaString = formulaNode.asExpression();

        // We need stable ids for our default columns, otherwise
        // the views will get confused and refresh unnecessarily
        String id = formulaString.replace('.', 'd');

        return ImmutableTableColumn.builder()
                .id(id)
                .formula(formulaString)
                .build();
    }

    private void addKeyColumns(List<EffectiveTableColumn> columns, FormTree.Node node) {

        LookupKeySet lookupKeySet = new LookupKeySet(formTree, node.getField());
        Map<LookupKey, FormulaNode> formulas = lookupKeySet.getKeyFormulas(node.getFieldId());

        for (LookupKey lookupKey : lookupKeySet.getLookupKeys()) {

            ImmutableTableColumn tableColumn = ImmutableTableColumn.builder()
                .id(node.getFieldId().asString() + "_k" + lookupKey.getKeyIndex())
                .formula(formulas.get(lookupKey).toString())
                .label(lookupKey.getKeyLabel())
                .build();

            columns.add(new EffectiveTableColumn(formTree, tableColumn));
        }
    }

    /**
     * Returns true if this is a sub table view that is linked to a parent view.
     */
    public boolean isSubTable() {
        return selectedParentRef.isPresent();
    }

    public ResourceId getFormId() {
        return formTree.getRootFormId();
    }

    private QueryModel buildQuery(List<EffectiveTableColumn> columns) {
        QueryModel queryModel = new QueryModel(formTree.getRootFormId());
        if(tableModel.getFilter().isPresent()) {
            queryModel.setFilter(tableModel.getFilter().get());
        }
        for (SortModel sortModel : tableModel.getSorting()) {
            queryModel.addSortModel(sortModel);
        }
        queryModel.selectResourceId().as(ID_COLUMN_ID);
        for (EffectiveTableColumn column : columns) {
            queryModel.addColumns(column.getQueryModel());
        }
        return queryModel;
    }

    private QueryModel buildQuery(List<EffectiveTableColumn> columns, RecordRef recordRef) {
        QueryModel queryModel = buildQuery(columns);
        queryModel.setFilter(Formulas.equals(new SymbolNode("@parent"), new ConstantNode(recordRef.getRecordId().asString())));

        return queryModel;
    }

    private ColumnSet emptyColumnSet(List<EffectiveTableColumn> columns) {

        Map<String, ColumnView> columnMap = new HashMap<>();
        for (EffectiveTableColumn column : columns) {
            for (ColumnModel columnModel : column.getQueryModel()) {
                columnMap.put(columnModel.getId(), new EmptyColumnView(ColumnType.STRING, 0));
            }
        }

        return new ColumnSet(0, columnMap);
    }

    public FormTree getFormTree() {
        return formTree;
    }

    public List<EffectiveTableColumn> getColumns() {
        return columns;
    }

    public Observable<ColumnSet> getColumnSet() {
        return columnSet;
    }

    public String getFormLabel() {
        if(formTree.getRootState() == FormTree.State.VALID) {
            return formTree.getRootFormClass().getLabel();
        } else {
            return "";
        }
    }
}
