package org.activityinfo.ui.client.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.dnd.core.client.ListViewDragSource;
import com.sencha.gxt.dnd.core.client.ListViewDropTarget;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.IconButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.DualListField;
import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.analysis.viewModel.FormForest;
import org.activityinfo.ui.client.measureDialog.view.FieldTreeView;
import org.activityinfo.ui.client.measureDialog.view.MeasureTreeNode;
import org.activityinfo.ui.client.table.view.Dialogs;
import org.activityinfo.ui.client.table.view.DoubleClickEditTextCell;

import java.util.*;
import java.util.function.Consumer;

/**
 * Allows users to select which columns appear in the table
 */
public class ColumnDialog {

    private static final DualListField.DualListFieldAppearance APPEARANCE =
            GWT.create(DualListField.DualListFieldAppearance.class);
    public static final String DRAG_DROP_GROUP = "columns";
    private final FieldTreeView fieldTreeView;
    private EffectiveTableModel tableModel;

    private static class TableColumnModel {
        private final TableColumn model;
        private String id;
        private String effectiveLabel;
        private String updatedLabel;

        public TableColumnModel(EffectiveTableColumn column) {
            this.id = column.getId();
            this.effectiveLabel = column.getLabel();
            this.model = column.getModel();
        }

        public void setUpdatedLabel(String updatedLabel) {
            this.updatedLabel = updatedLabel;
        }

        public TableColumn updatedModel() {
            TableColumn updatedModel = model;
            // Only updated the label if it has been explicitly changed.
            // Otherwise leave it implicit so that it will reflect changes to the
            // field's label
            if(updatedLabel != null && !updatedLabel.equals(effectiveLabel)) {
                updatedModel = ImmutableTableColumn.builder()
                                .from(updatedModel)
                                .label(updatedLabel)
                                .build();
            }
            return updatedModel;
        }
    }

    private Dialog dialog;

    private ListStore<TableColumnModel> selectedStore;
    private ListView<TableColumnModel, String> selectedList;

    public ColumnDialog(EffectiveTableModel tableModel) {
        this.tableModel = tableModel;
        fieldTreeView = new FieldTreeView(Observable.just(new FormForest(tableModel.getFormTree())));
        fieldTreeView.setBorders(true);


        selectedStore = new ListStore<>(column -> column.id);
        selectedStore.setAutoCommit(true);

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
                object.setUpdatedLabel(value);
            }

            @Override
            public String getPath() {
                return "label";
            }
        });
        selectedList.setBorders(true);
        selectedList.setCell(new DoubleClickEditTextCell());

        // Set up drag and drop reordering for columns
        ListViewDragSource<TableColumnModel> selectedDragSource = new ListViewDragSource<>(selectedList);
        selectedDragSource.setGroup(DRAG_DROP_GROUP);

        ListViewDropTarget<TableColumnModel> selectedTarget = new ListViewDropTarget<>(selectedList);
        selectedTarget.setGroup(DRAG_DROP_GROUP);
        selectedTarget.setAllowSelfAsSource(true);

        IconButton up = new IconButton(APPEARANCE.up());
        up.addSelectHandler(this::onUp);

        IconButton addButton = new IconButton(APPEARANCE.right());
      //  right.setToolTip(getMessages().addSelected());
        addButton.addSelectHandler(this::addColumns);

        IconButton removeButton = new IconButton(APPEARANCE.left());
        removeButton.addSelectHandler(this::removeColumns);

        IconButton removeAll = new IconButton(APPEARANCE.allLeft());
      //  removeAll.setToolTip(getMessages().removeAll());
        removeAll.addSelectHandler(this::clearColumns);

        IconButton down = new IconButton(APPEARANCE.down());
        down.addSelectHandler(this::onDown);

        VerticalPanel buttonBar = new VerticalPanel();
        buttonBar.setSpacing(3);
        buttonBar.getElement().getStyle().setProperty("margin", "7px");
        buttonBar.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonBar.add(up);
        buttonBar.add(addButton);
        buttonBar.add(removeButton);
        buttonBar.add(removeAll);
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

    private void addColumns(SelectEvent event) {
        List<MeasureTreeNode> selectedItems = fieldTreeView.getSelectionModel().getSelectedItems();
        for (MeasureTreeNode measureTreeNode : selectedItems) {
            Optional<TableColumn> tableColumn = measureTreeNode.newTableColumn();
            if (tableColumn.isPresent()) {
                selectedStore.add(new TableColumnModel(new EffectiveTableColumn(tableModel.getFormTree(), tableColumn.get())));
            }
        }

        // Advance the tree selection to the next item.
        if(selectedItems.size() == 1) {
            fieldTreeView.selectNext(selectedItems.get(0));
        }

    }

    private void removeColumns(SelectEvent event) {
        for (TableColumnModel tableColumnModel : selectedList.getSelectionModel().getSelectedItems()) {
            selectedStore.remove(tableColumnModel);
        }
    }

    private void clearColumns(SelectEvent event) {
        selectedStore.clear();
    }


    private void onUp(SelectEvent event) {
        selectedList.moveSelectedUp();
    }

    private void onDown(SelectEvent event) {
        selectedList.moveSelectedDown();
    }

    public void show(Consumer<TableModel> updateHandler) {
        dialog.show();
        Dialogs.addCallback(dialog, updateHandler, this::buildUpdatedModel);
    }

    private TableModel buildUpdatedModel() {

        List<TableColumn> columns = new ArrayList<>();
        for (TableColumnModel model : selectedStore.getAll()) {
            columns.add(model.updatedModel());
        }

        return ImmutableTableModel.builder()
            .from(this.tableModel.getModel())
            .columns(columns)
            .build();
    }

}
