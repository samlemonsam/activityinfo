package org.activityinfo.analysis.table;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.expr.Exprs;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.query.shared.FormSource;

import javax.annotation.Nullable;
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
    private Optional<Observable<Optional<RecordRef>>> selectedParentRef;
    private List<EffectiveTableColumn> columns;
    private Observable<ColumnSet> columnSet;

    public EffectiveTableModel(FormSource formSource, FormTree formTree, TableModel tableModel) {
        this(formSource, formTree, tableModel, Optional.absent());
    }

    public EffectiveTableModel(FormSource formSource, FormTree formTree, TableModel tableModel,
                               Optional<Observable<Optional<RecordRef>>> selectedParentRef) {
        this.formTree = formTree;
        this.selectedParentRef = selectedParentRef;
        this.columnSet = new StatefulValue<>();
        this.columns = new ArrayList<>();

        if(formTree.getRootState() == FormTree.State.VALID) {
            if (this.columns.isEmpty()) {
                addDefaultColumns(formTree);
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


    private void addDefaultColumns(FormTree formTree) {
        if(!isSubTable() && formTree.getRootFormClass().isSubForm()) {
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
               type instanceof SerialNumberType ||
               type instanceof QuantityType ||
               type instanceof BarcodeType ||
               type instanceof EnumType ||
               type instanceof GeoPointType ||
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

    /**
     * Returns true if this is a sub table view that is linked to a parent view.
     */
    public boolean isSubTable() {
        return selectedParentRef.isPresent();
    }

    private boolean shouldIncludeReference(FormTree.Node childNode) {
        if(childNode.isParentReference()) {
            if(isSubTable()) {
                // Do not include parent fields when the sub form is displayed
                // as a sub table linked to its parent.
                return false;
            }
        }
        return true;
    }

    public ResourceId getFormId() {
        return formTree.getRootFormId();
    }

    private QueryModel buildQuery(List<EffectiveTableColumn> columns) {
        QueryModel queryModel = new QueryModel(formTree.getRootFormId());
        queryModel.selectResourceId().as(ID_COLUMN_ID);
        for (EffectiveTableColumn column : columns) {
            queryModel.addColumns(column.getQueryModel());
        }
        return queryModel;
    }

    private QueryModel buildQuery(List<EffectiveTableColumn> columns, RecordRef recordRef) {
        QueryModel queryModel = buildQuery(columns);
        queryModel.setFilter(Exprs.equals(new SymbolExpr("@parent"), new ConstantExpr(recordRef.getRecordId().asString())));

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
