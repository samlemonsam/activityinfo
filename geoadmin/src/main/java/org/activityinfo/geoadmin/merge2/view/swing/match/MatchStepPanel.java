package org.activityinfo.geoadmin.merge2.view.swing.match;


import org.activityinfo.geoadmin.merge2.model.InstanceMatchSet;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.match.MatchRow;
import org.activityinfo.geoadmin.merge2.view.match.MatchTableColumn;
import org.activityinfo.geoadmin.merge2.view.match.ResolutionColumn;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.observable.SubscriptionSet;

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
    
    private ImportView viewModel;

    private final MatchTableModel tableModel;
    private final JTable table;

    private final SubscriptionSet subscriptions = new SubscriptionSet();
    private List<MatchTableColumn> columns;

    public MatchStepPanel(ImportView view) {
        this.viewModel = view;
        
        setLayout(new BorderLayout());

        TableColumnModel tableColumnModel = new DefaultTableColumnModel();

        JTableHeader tableHeader = new JTableHeader(tableColumnModel);
        tableHeader.setResizingAllowed(true);
        
        tableModel = new MatchTableModel(viewModel);
        table = new JTable(tableModel);
        table.setAutoCreateColumnsFromModel(false);
        table.setColumnModel(tableColumnModel);
        table.setTableHeader(tableHeader);
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

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(columns != null) {
                    MatchTableColumn column = columns.get(table.getSelectedColumn());
                    if(column instanceof ResolutionColumn) {
                        toggleResolution(table.getSelectedRow());
                    } else {
                     //   showContextMenu(e);
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

        JPanel statusPanel = new JPanel();
        statusPanel.add(unresolvedCount);
        
        add(statusPanel, BorderLayout.PAGE_END);
    }


    private void toggleResolution(int selectedRow) {
        InstanceMatchSet instanceMatchSet = viewModel.getModel().getInstanceMatchSet();
        MatchRow row = viewModel.getMatchTable().get(table.convertRowIndexToModel(selectedRow));

        if(row.isMatched()) {
            if(!row.isResolved()) {
                instanceMatchSet.add(row.asInstanceMatch());
            } else {
                instanceMatchSet.remove(row.asInstanceMatch());
            }
        }
    }

    private void unmatch(MatchRow row) {
        
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
            newColumn.setHeaderValue(columns.get(i).getHeader());
            newColumn.setCellRenderer(new MatchColumnRenderer(columns.get(i)));
            table.addColumn(newColumn);
        }

        tableModel.updateColumns(columns);

//        TableColumnModel tableColumnModel = buildColumnModel(columns);
//        table.setColumnModel(tableColumnModel);
//
//        JTableHeader tableHeader = new JTableHeader(tableColumnModel);
//        tableHeader.setResizingAllowed(true);
//   //     tableHeader.setReorderingAllowed(false);
//        table.setTableHeader(tableHeader);

    }


    @Override
    public void stop() {
        subscriptions.unsubscribeAll();
        tableModel.stop();
    }
}
