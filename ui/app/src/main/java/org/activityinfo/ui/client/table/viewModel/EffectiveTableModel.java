package org.activityinfo.ui.client.table.viewModel;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.List;

/**
 * The effective description of a table
 */
public class EffectiveTableModel {

    public static final String ID_COLUMN_ID = "$$id";

    private FormTree formTree;
    private List<EffectiveColumn> columns;
    private Observable<ColumnSet> columnSet;

    public EffectiveTableModel(FormStore formStore, FormTree formTree) {
        this.formTree = formTree;
        this.columnSet = new StatefulValue<>();
        this.columns = new ArrayList<>();

        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.getType() instanceof TextType) {
                columns.add(new EffectiveColumn(node));
            } else if(node.getType() instanceof QuantityType) {
                columns.add(new EffectiveColumn(node));
            }
        }

        this.columnSet = formStore.query(buildQuery(columns));
    }

    public ResourceId getFormId() {
        return formTree.getRootFormClass().getId();
    }

    private QueryModel buildQuery(List<EffectiveColumn> columns) {
        QueryModel queryModel = new QueryModel(formTree.getRootFormClass().getId());
        queryModel.selectResourceId().as(ID_COLUMN_ID);
        for (EffectiveColumn column : columns) {
            queryModel.addColumn(column.getQueryModel());
        }
        return queryModel;
    }

    public FormTree getFormTree() {
        return formTree;
    }

    public List<EffectiveColumn> getColumns() {
        return columns;
    }

    public Observable<ColumnSet> getColumnSet() {
        return columnSet;
    }

    public String getFormLabel() {
        return formTree.getRootFormClass().getLabel();
    }
}
