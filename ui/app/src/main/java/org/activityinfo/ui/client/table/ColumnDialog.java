package org.activityinfo.ui.client.table;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.tree.Tree;
import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.analysis.viewModel.FormForest;
import org.activityinfo.ui.client.measureDialog.view.FieldTreeView;

import java.util.Arrays;

/**
 * Allows users to select which columns appear in the table
 */
public class ColumnDialog {

    private final Dialog dialog;

    private static class TableColumnModel {
        private String id;
        private String effectiveLabel;

        public TableColumnModel(EffectiveTableColumn column) {
            this.id = column.getId();
            this.effectiveLabel = column.getLabel();
        }
    }


    private TreeStore<TableColumnModel> availableStore;
    private Tree<TableColumnModel, String> availableTree;

    private ListStore<TableColumnModel> selectedStore;
    private Grid<TableColumnModel> selectedGrid;


    public ColumnDialog(EffectiveTableModel tableModel) {

        FieldTreeView fieldTreeView = new FieldTreeView(Observable.just(new FormForest(tableModel.getFormTree())));


        selectedStore = new ListStore<>(column -> column.id);
        for (EffectiveTableColumn effectiveTableColumn : tableModel.getColumns()) {
            selectedStore.add(new TableColumnModel(effectiveTableColumn));
        }

        selectedGrid = new Grid<>(selectedStore, selectedGridModel());
        selectedGrid.getView().setAutoExpandColumn(selectedGrid.getColumnModel().getColumn(0));
        selectedGrid.getView().setAutoExpandMax(Integer.MAX_VALUE);

        HorizontalLayoutContainer container = new HorizontalLayoutContainer();
        container.setHeight(-1);
        container.add(fieldTreeView, new HorizontalLayoutContainer.HorizontalLayoutData(0.5, 1, new Margins(5, 5, 5, 5)));
        container.add(selectedGrid, new HorizontalLayoutContainer.HorizontalLayoutData(0.5, 1, new Margins(5, 5, 5, 5)));

        dialog = new Dialog();
        dialog.setHeading(I18N.CONSTANTS.chooseColumns());
        dialog.add(container);
    }

    public void show() {
        dialog.show();
    }

    private ColumnModel<TableColumnModel> selectedGridModel() {

        ColumnConfig<TableColumnModel, String> labelColumn = new ColumnConfig<>(new ValueProvider<TableColumnModel, String>() {
            @Override
            public String getValue(TableColumnModel object) {
                return object.effectiveLabel;
            }

            @Override
            public void setValue(TableColumnModel object, String value) {
                object.effectiveLabel = value;
            }

            @Override
            public String getPath() {
                return "label";
            }
        });
        labelColumn.setHeader("Label");
        labelColumn.setCell(new TextInputCell());
        return new ColumnModel<>(Arrays.asList(labelColumn));
    }

}
