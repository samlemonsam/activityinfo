package org.activityinfo.ui.client.component.table.dialog;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SimpleKeyProvider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTableStyle;
import org.activityinfo.ui.client.component.table.InstanceTableView;
import org.activityinfo.ui.client.style.table.DataGridResources;
import org.activityinfo.ui.client.widget.ModalDialog;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author yuriyz on 3/21/14.
 */
public class ChooseColumnsDialog {

    interface ChooseColumnsDialogUiBinder extends UiBinder<HTMLPanel, ChooseColumnsDialog> {
    }

    private static ChooseColumnsDialogUiBinder uiBinder = GWT.create(ChooseColumnsDialogUiBinder.class);

    private final ModalDialog dialog;
    private final InstanceTableView tableView;

    private final ListDataProvider<FieldColumn> selectedTableDataProvider = new ListDataProvider<>();
    private final MultiSelectionModel<FieldColumn> selectedSelectionModel = new MultiSelectionModel<>(
            new SimpleKeyProvider<FieldColumn>());
    private final DataGrid<FieldColumn> selectedTable;

    private final ListDataProvider<FieldColumn> tableDataProvider = new ListDataProvider<>();
    private final MultiSelectionModel<FieldColumn> selectionModel = new MultiSelectionModel<>(
            new SimpleKeyProvider<FieldColumn>());
    private final DataGrid<FieldColumn> table;

    @UiField
    HTMLPanel selectedColumnTableContainer;
    @UiField
    HTMLPanel columnTableContainer;
    @UiField
    Button rightButton;
    @UiField
    Button leftButton;
    @UiField
    TextBox filterColumnTable;
    @UiField
    Button upButton;
    @UiField
    Button downButton;


    public ChooseColumnsDialog(final InstanceTableView tableView) {
        this.tableView = tableView;

        DataGridResources.INSTANCE.dataGridStyle().ensureInjected();

        dialog = new ModalDialog(uiBinder.createAndBindUi(this));
        dialog.setDialogTitle(I18N.CONSTANTS.chooseColumns());
        dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onOk();
            }
        });

        selectedTable = createTable();
        selectedTable.setSelectionModel(selectedSelectionModel);
        selectedTableDataProvider.addDataDisplay(selectedTable);
        selectedTableDataProvider.setList(tableView.getSelectedColumns());
        selectedTableDataProvider.refresh();
        selectedTable.addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(final DoubleClickEvent event) {
                Set<FieldColumn> set = selectedSelectionModel.getSelectedSet();
                if (event.getSource() != null && set.size() == 1) {
                    onMoveLeft(null);
                }
            }
        }, DoubleClickEvent.getType());

        final List<FieldColumn> allColumns = Lists.newArrayList(tableView.getColumns());

        table = createTable();
        table.setSelectionModel(selectionModel);
        table.setRowStyles(new RowStyles<FieldColumn>() {
            @Override
            public String getStyleNames(FieldColumn row, int rowIndex) {
                if (selectedTableDataProvider.getList().contains(row)) {
                    return InstanceTableStyle.INSTANCE.rowPresent();
                }
                return null;
            }
        });
        table.addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(final DoubleClickEvent event) {
                Set<FieldColumn> set = selectionModel.getSelectedSet();
                if (event.getSource() != null && set.size() == 1) {
                    onMoveRight(null);
                }
            }
        }, DoubleClickEvent.getType());
        tableDataProvider.addDataDisplay(table);
        tableDataProvider.setList(allColumns);
        tableDataProvider.refresh();

        selectedColumnTableContainer.add(selectedTable);
        columnTableContainer.add(table);

        setMoveRightButtonState();
        setRemoveButtonState();
        setUpButtonState();
        setDownButtonState();

        selectedSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                setRemoveButtonState();
                setMoveRightButtonState();
                setDownButtonState();
                setUpButtonState();
            }
        });
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                setMoveRightButtonState();
                setRemoveButtonState();
            }
        });
        filterColumnTable.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                final String value = filterColumnTable.getValue();
                final ArrayList<FieldColumn> columnsToShow = Lists.newArrayList();
                for (FieldColumn column : tableView.getColumns()) {
                    final String headerLowercased = column.getHeader().toLowerCase();
                    if (Strings.isNullOrEmpty(value) || headerLowercased.contains(value.toLowerCase())) {
                        columnsToShow.add(column);
                    }
                }

                tableDataProvider.setList(columnsToShow);
                tableDataProvider.refresh();
            }
        });
    }

    public void show() {
        dialog.show();
    }

    private static DataGrid<FieldColumn> createTable() {
        final Column<FieldColumn, String> labelColumn = new Column<FieldColumn, String>(
                new TextCell()) {
            @Override
            public String getValue(FieldColumn object) {
                return object.getHeader();
            }
        };
        labelColumn.setSortable(false);

        final DataGrid<FieldColumn> table = new DataGrid<>(1000, DataGridResources.INSTANCE);
        table.setHeight("300px"); // need to avoid height hardcode
        table.setEmptyTableWidget(new Label());
        table.setAutoHeaderRefreshDisabled(true);
        table.setAutoFooterRefreshDisabled(true);
        table.setSkipRowHoverCheck(true);
        table.setSkipRowHoverFloatElementCheck(true);
        table.addColumn(labelColumn);
        table.setColumnWidth(labelColumn, 100, Style.Unit.PCT);
        return table;
    }

    private void onOk() {
        tableView.setSelectedColumns(Lists.newArrayList(selectedTableDataProvider.getList()));
        dialog.hide();
    }

    public void setMoveRightButtonState() {
        final HashSet<FieldColumn> selectedSet = Sets.newHashSet(selectionModel.getSelectedSet());
        selectedSet.removeAll(selectedTableDataProvider.getList());
        rightButton.setEnabled(!selectedSet.isEmpty());
    }

    public void setRemoveButtonState() {
        leftButton.setEnabled(!selectedSelectionModel.getSelectedSet().isEmpty());
    }

    public void setUpButtonState() {
        boolean enabled = false;
        if (!selectedSelectionModel.getSelectedSet().isEmpty()) {
            int minIndex = -1;
            for (FieldColumn column : selectedSelectionModel.getSelectedSet()) {
                final int indexOf = selectedTableDataProvider.getList().indexOf(column);
                if (minIndex > indexOf || minIndex == -1) {
                    minIndex = indexOf;
                }
            }
            if (minIndex > 0) {
                enabled = true;
            }
        }

        upButton.setEnabled(enabled);
    }

    public void setDownButtonState() {
        boolean enabled = false;
        if (!selectedSelectionModel.getSelectedSet().isEmpty()) {
            int maxIndex = -1;
            for (FieldColumn column : selectedSelectionModel.getSelectedSet()) {
                final int indexOf = selectedTableDataProvider.getList().indexOf(column);
                if (maxIndex < indexOf || maxIndex == -1) {
                    maxIndex = indexOf;
                }
            }
            if (maxIndex < (selectedTableDataProvider.getList().size() - 1)) {
                enabled = true;
            }
        }

        downButton.setEnabled(enabled);
    }

    @UiHandler("rightButton")
    public void onMoveRight(@Nullable ClickEvent event) {
        final Set<FieldColumn> set = Sets.newHashSet(selectionModel.getSelectedSet());

        set.removeAll(selectedTableDataProvider.getList());
        selectedTableDataProvider.getList().addAll(set);
        selectedTableDataProvider.refresh();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                setMoveRightButtonState();
                redrawTableRows(set);
            }
        });
    }

    public void redrawTableRows(Collection<FieldColumn> rows) {
        for (FieldColumn column : rows) {
            final int index = tableDataProvider.getList().indexOf(column);
            if (index != -1) {
                table.redrawRow(index);
            }
        }
    }

    @UiHandler("leftButton")
    public void onMoveLeft(@Nullable ClickEvent event) {
        final Set<FieldColumn> set = selectedSelectionModel.getSelectedSet();

        selectedTableDataProvider.getList().removeAll(set);
        selectedTableDataProvider.refresh();
        selectedSelectionModel.clear(); // remove selection explicitly

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                setUpButtonState();
                setDownButtonState();
                setRemoveButtonState();
                redrawTableRows(set);
            }
        });
    }

    @UiHandler("upButton")
    public void onUp(ClickEvent event) {
        final Set<FieldColumn> set = selectedSelectionModel.getSelectedSet();

        final ArrayList<FieldColumn> copy = Lists.newArrayList(selectedTableDataProvider.getList());
        for (FieldColumn column : set) {
            final int index = copy.indexOf(column);
            if (index > 0) {
                Collections.swap(copy, index, index - 1);
            }
        }
        selectedTableDataProvider.setList(copy);

        setUpButtonState();
    }

    @UiHandler("downButton")
    public void onDown(ClickEvent event) {
        final Set<FieldColumn> set = selectedSelectionModel.getSelectedSet();

        final ArrayList<FieldColumn> copy = Lists.newArrayList(selectedTableDataProvider.getList());
        final int size = copy.size();
        for (FieldColumn column : set) {
            final int index = copy.indexOf(column);
            if (index < (size - 1)) {
                Collections.swap(copy, index, index + 1);
            }
        }
        selectedTableDataProvider.setList(copy);

        setDownButtonState();
    }
}
