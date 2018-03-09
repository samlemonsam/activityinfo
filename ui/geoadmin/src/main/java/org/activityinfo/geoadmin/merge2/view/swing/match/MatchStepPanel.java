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
package org.activityinfo.geoadmin.merge2.view.swing.match;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.merge2.model.InstanceMatchSet;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.match.*;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;
import org.activityinfo.geoadmin.merge2.view.swing.match.select.SelectDialog;
import org.activityinfo.geoadmin.writer.TableWriter;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.observable.SubscriptionSet;
import org.activityinfo.observable.TableObserver;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static java.lang.String.format;

public class MatchStepPanel extends StepPanel {

    public static final String LOADING_COMPONENT = "loading";
    public static final String TABLE_COMPONENT = "table";
    public static final int SEPARATOR_COLUMN_WIDTH = 25;

    private final ImportView viewModel;
    private final SubscriptionSet subscriptions = new SubscriptionSet();

    private final JTable table;
    private final MatchTableModel tableModel;
    private final MatchTableSelection tableSelection;
    private final CellRenderers cellRenderers;
    
    private List<MatchTableColumn> columns;


    public MatchStepPanel(final ImportView view) {
        this.viewModel = view;
        
        setLayout(new BorderLayout());

        cellRenderers = new CellRenderers(view.getMatchTable());
        
        final TableColumnModel tableColumnModel = new DefaultTableColumnModel();
        
        
        JTableHeader tableHeader = new JTableHeader(tableColumnModel);
        tableHeader.setResizingAllowed(true);
        
        tableModel = new MatchTableModel(viewModel);
        
        tableSelection = new MatchTableSelection();
        
        table = new JTable(tableModel);
        table.setAutoCreateColumnsFromModel(false);
        table.setColumnModel(tableColumnModel);
        table.setTableHeader(tableHeader);
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setAutoCreateRowSorter(true);
        

        JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
        
        final JPanel tableContainer = new JPanel(new CardLayout());
        tableContainer.add(loadingLabel, LOADING_COMPONENT);
        tableContainer.add(new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), TABLE_COMPONENT);
        
        add(tableContainer, BorderLayout.CENTER);


        subscriptions.add(viewModel.getMatchTable().getColumns().subscribe(new Observer<List<MatchTableColumn>>() {
            @Override
            public void onChange(Observable<List<MatchTableColumn>> observable) {
                if (observable.isLoading()) {
                    ((CardLayout) tableContainer.getLayout()).show(tableContainer, LOADING_COMPONENT);
                } else {
                    ((CardLayout) tableContainer.getLayout()).show(tableContainer, TABLE_COMPONENT);
                    columns = observable.get();
                    onColumnsChanged();
                }
            }
        }));
        

        subscriptions.add(viewModel.getMatchTable().subscribe(new TableObserver() {
            @Override
            public void onRowsChanged() {
                tableModel.fireTableDataChanged();
            }

            @Override
            public void onRowChanged(int index) {
                tableModel.fireTableDataChanged();
            }
        }));
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(columns != null && table.getSelectedColumn() != -1) {
                    MatchTableColumn column = columns.get(table.getSelectedColumn());
                    if(column instanceof ResolutionColumn) {
                        toggleResolution(table.getSelectedRow());
                    } else if(e.getClickCount() == 2) {
                        int viewRow = table.rowAtPoint(e.getPoint());
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        int viewColumn = table.columnAtPoint(e.getPoint());
                        int modelColumn = table.convertColumnIndexToModel(viewColumn);
                        onDoubleClick(modelRow, modelColumn);
                    }
                }
            }
        });
        
        final JLabel unresolvedCount = new JLabel();
        subscriptions.add(viewModel.getMatchTable().getUnresolvedCount().subscribe(new Observer<Integer>() {
            @Override
            public void onChange(Observable<Integer> observable) {
                if(!observable.isLoading()) {
                    if(observable.get() == 0) {
                        unresolvedCount.setText("All matches resolved.");
                    } else {
                        unresolvedCount.setText(format("%d unresolved match(es).", observable.get()));
                    }
                }
            }
        }));
        
        unresolvedCount.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    TableWriter.export(tableModel);
                }
            }
        });
        
        JPanel statusPanel = new JPanel();
        statusPanel.add(unresolvedCount);
        
        add(statusPanel, BorderLayout.PAGE_END);
    }

    private void onDoubleClick(int modelRow, int modelColumn) {
        MatchRow matchRow = viewModel.getMatchTable().get(modelRow);
        Optional<MatchSide> side = columns.get(modelColumn).getSide();
        if(side.isPresent()) {
            MatchSide sideBeingMatchedAgainst = side.get();
            if(!matchRow.isMatched(sideBeingMatchedAgainst)) {
                sideBeingMatchedAgainst = sideBeingMatchedAgainst.opposite();
            }
            SelectDialog dialog = new SelectDialog(this, viewModel, modelRow, sideBeingMatchedAgainst);
            dialog.setVisible(true);
        }
    }


    private void toggleResolution(int selectedRow) {
        InstanceMatchSet instanceMatchSet = viewModel.getModel().getInstanceMatchSet();
        MatchRow row = viewModel.getMatchTable().get(table.convertRowIndexToModel(selectedRow));

        if(row.isMatched()) {
            // The match is iffy, so confirm it by adding an explicit pairing between the
            // matches
            if(!row.isResolved()) {
                instanceMatchSet.add(row.asInstanceMatch());
            } else {
                instanceMatchSet.remove(row.asInstanceMatch());
            }
        } else {
            if(!row.isMatched(MatchSide.SOURCE)) {
                // An instance in the target collection has not been matched
                // and will be deleted. Confirm this by add an explicit non-matching
                instanceMatchSet.add(row.asInstanceMatch());
            } else {
                // An instance in the source has not been matched and will be added to the 
                // target collection
                instanceMatchSet.add(row.asInstanceMatch());
            }
        }
    }


    private void onColumnsChanged() {


        // Remove any current columns
        TableColumnModel cm = table.getColumnModel();
        while (cm.getColumnCount() > 0) {
            cm.removeColumn(cm.getColumn(0));
        }

        // Create new columns from the data model info
        for (int i = 0; i < columns.size(); i++) {
            TableColumn newColumn = new TableColumn(i);
            MatchTableColumn matchColumn = columns.get(i);
            newColumn.setHeaderValue(matchColumn.getHeader());
            newColumn.setCellRenderer(cellRenderers.rendererFor(matchColumn));
            if(matchColumn instanceof SeparatorColumn) {
                newColumn.setResizable(false);
                newColumn.setWidth(SEPARATOR_COLUMN_WIDTH);
                newColumn.setMaxWidth(SEPARATOR_COLUMN_WIDTH);
                newColumn.setMinWidth(SEPARATOR_COLUMN_WIDTH);
            }
            table.addColumn(newColumn);
        }

        tableModel.updateColumns(columns);
        
    }


    @Override
    public void stop() {
        subscriptions.unsubscribeAll();
        tableModel.stop();
    }
}
