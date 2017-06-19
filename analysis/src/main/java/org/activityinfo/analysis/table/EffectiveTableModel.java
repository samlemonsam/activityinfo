package org.activityinfo.analysis.table;

import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.query.shared.FormSource;

import java.util.ArrayList;
import java.util.List;

/**
 * The effective description of a table
 */
public class EffectiveTableModel {


    public static final String ID_COLUMN_ID = "$$id";

    private FormTree formTree;
    private List<EffectiveTableColumn> columns;
    private Observable<ColumnSet> columnSet;

    public EffectiveTableModel(FormSource formSource, FormTree formTree, TableModel tableModel) {
        this.formTree = formTree;
        this.columnSet = new StatefulValue<>();
        this.columns = new ArrayList<>();

        if(formTree.getRootState() == FormTree.State.VALID) {
            if (this.columns.isEmpty()) {
                addDefaultColumns(formTree);
            }
        }

        this.columnSet = formSource.query(buildQuery(columns));
    }

    private void addDefaultColumns(FormTree formTree) {
        if(formTree.getRootFormClass().isSubForm()) {
            addDefaultColumns(formTree.parentTree());
        }
        for (FormTree.Node node : formTree.getRootFields()) {
            if (isSimple(node.getType())) {
                columns.add(new EffectiveTableColumn(formTree, columnModel(node)));

            } else if (node.getType() instanceof ReferenceType) {
                addKeyColumns(columns,  node);
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
               type instanceof QuantityType ||
               type instanceof BarcodeType ||
               (type instanceof EnumType && ((EnumType) type).getCardinality() == Cardinality.SINGLE) ||
               type instanceof LocalDateType ||
               type instanceof CalculatedFieldType;
    }

    private ImmutableTableColumn columnModel(FormTree.Node node) {
        return ImmutableTableColumn.builder()
                .formula(node.getPath().toExpr().asExpression())
                .build();
    }

    private void addKeyColumns(List<EffectiveTableColumn> columns, FormTree.Node node) {


        // First add reference key fields
        for (FormTree.Node childNode : node.getChildren()) {
            if(childNode.getField().isKey() && childNode.isReference()) {
                addKeyColumns(columns, childNode);
            }
        }

        // Now any non-reference key fields
        for (FormTree.Node childNode : node.getChildren()) {
            if(childNode.getField().isKey() && !childNode.isReference()) {
                columns.add(new EffectiveTableColumn(formTree, columnModel(childNode)));
            }
        }
    }

    public ResourceId getFormId() {
        return formTree.getRootFormId();
    }

    private QueryModel buildQuery(List<EffectiveTableColumn> columns) {
        QueryModel queryModel = new QueryModel(formTree.getRootFormId());
        queryModel.selectResourceId().as(ID_COLUMN_ID);
        for (EffectiveTableColumn column : columns) {
            queryModel.addColumn(column.getQueryModel());
        }
        return queryModel;
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
