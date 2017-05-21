package org.activityinfo.ui.client.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.IconButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.DualListField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.tree.Tree;
import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.analysis.viewModel.FormForest;
import org.activityinfo.ui.client.measureDialog.view.FieldTreeView;

import java.util.Arrays;

/**
 * Allows users to select which columns appear in the table
 */
public class ColumnDialog {

    private static final DualListField.DualListFieldAppearance APPEARANCE =
            GWT.create(DualListField.DualListFieldAppearance.class);

    private static class TableColumnModel {
        private String id;
        private String effectiveLabel;

        public TableColumnModel(EffectiveTableColumn column) {
            this.id = column.getId();
            this.effectiveLabel = column.getLabel();
        }
    }

    private Dialog dialog;

    private TreeStore<TableColumnModel> availableStore;
    private Tree<TableColumnModel, String> availableTree;

    private ListStore<TableColumnModel> selectedStore;
    private ListView<TableColumnModel, String> selectedList;


    public ColumnDialog(EffectiveTableModel tableModel) {

        FieldTreeView fieldTreeView = new FieldTreeView(Observable.just(new FormForest(tableModel.getFormTree())));
        fieldTreeView.setBorders(true);

        selectedStore = new ListStore<>(column -> column.id);
        for (EffectiveTableColumn effectiveTableColumn : tableModel.getColumns()) {
            selectedStore.add(new TableColumnModel(effectiveTableColumn));
        }

        selectedList = new ListView<>(selectedStore, new ValueProvider<TableColumnModel, String>() {
            @Override
            public String getValue(TableColumnModel object) {
                return object.effectiveLabel;
            }

            @Override
            public void setValue(TableColumnModel object, String value) {
            }

            @Override
            public String getPath() {
                return "label";
            }
        });
        selectedList.setBorders(true);



        IconButton up = new IconButton(APPEARANCE.up());
//        up.setToolTip(getMessages().moveUp());
        up.addSelectHandler(this::onUp);

        IconButton allRight = new IconButton(APPEARANCE.allRight());
        //allRight.setToolTip(getMessages().addAll());
//        allRight.addSelectHandler(new SelectEvent.SelectHandler() {
//            @Override
//            public void onSelect(SelectEvent event) {
//                onAllRight();
//            }
//        });

        IconButton right = new IconButton(APPEARANCE.right());
//      //  right.setToolTip(getMessages().addSelected());
//        right.addSelectHandler(new SelectEvent.SelectHandler() {
//
//            @Override
//            public void onSelect(SelectEvent event) {
//                onRight();
//            }
//        });

        IconButton left = new IconButton(APPEARANCE.left());
        //left.setToolTip(getMessages().removeSelected());
//        left.addSelectHandler(new SelectEvent.SelectHandler() {
//            @Override
//            public void onSelect(SelectEvent event) {
//                onLeft();
//            }
//        });

        IconButton allLeft = new IconButton(APPEARANCE.allLeft());
      //  allLeft.setToolTip(getMessages().removeAll());
//        allLeft.addSelectHandler(new SelectEvent.SelectHandler() {
//            @Override
//            public void onSelect(SelectEvent event) {
//                onAllLeft();
//            }
//        });

        IconButton down = new IconButton(APPEARANCE.down());
        //down.setToolTip(getMessages().moveDown());
//        down.addSelectHandler(new SelectEvent.SelectHandler() {
//
//            @Override
//            public void onSelect(SelectEvent event) {
//                onDown();
//            }
//        });

        VerticalPanel buttonBar = new VerticalPanel();
        buttonBar.setSpacing(3);
        buttonBar.getElement().getStyle().setProperty("margin", "7px");
        buttonBar.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonBar.add(up);
        buttonBar.add(allRight);
        buttonBar.add(right);
        buttonBar.add(left);
        buttonBar.add(allLeft);
        buttonBar.add(down);

        HorizontalLayoutContainer container = new HorizontalLayoutContainer();
        container.setHeight(-1);
        container.add(fieldTreeView, new HorizontalLayoutContainer.HorizontalLayoutData(0.5, 1, new Margins(5, 5, 5, 5)));
        container.add(buttonBar, new HorizontalLayoutContainer.HorizontalLayoutData(-1, -1));
        container.add(selectedList, new HorizontalLayoutContainer.HorizontalLayoutData(0.5, 1, new Margins(5, 5, 5, 5)));

        dialog = new Dialog();
        dialog.setPixelSize(650, 400);
        dialog.setHeading(I18N.CONSTANTS.chooseColumns());
        dialog.add(container);
    }

    private void onUp(SelectEvent event) {

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
