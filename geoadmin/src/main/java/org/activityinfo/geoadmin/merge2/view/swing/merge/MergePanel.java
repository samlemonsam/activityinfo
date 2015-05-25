package org.activityinfo.geoadmin.merge2.view.swing.merge;


import org.activityinfo.geoadmin.merge2.MergeModelStore;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MergePanel extends StepPanel {

    private final DefaultTableModel tableModel;
    private final JSplitPane horizontalSplitPane;
    private final UnmatchedTable sourceTable;
    private final UnmatchedTable targetTable;

    public MergePanel(MergeModelStore store) {
        setLayout(new BorderLayout());

        sourceTable = new UnmatchedTable(MergeSide.SOURCE, store.getFieldMapping());
        targetTable = new UnmatchedTable(MergeSide.TARGET, store.getFieldMapping());
        
        sourceTable.syncColumnsWith(targetTable);
        targetTable.syncColumnsWith(sourceTable);
        
        tableModel = new DefaultTableModel(new String[3][0], new String[]{"A", "B", "C"});

        horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createMatchedPanel(),
                new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        createUnmatched(targetTable),
                        createUnmatched(sourceTable)));
        
        add(horizontalSplitPane, BorderLayout.CENTER);

        
    }

    /**
     * Table which shows the combinations of rows that will be merged
     */
    private JPanel createMatchedPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        JLabel headingLabel = new JLabel("Matched Rows");
        panel.add(headingLabel, BorderLayout.PAGE_START);
        
        
        JTable table = new JTable(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createUnmatched(UnmatchedTable tableModel) {
        JPanel sourcePanel = new JPanel(new BorderLayout());

        JLabel headingLabel = new JLabel(tableModel.getHeader());
        sourcePanel.add(headingLabel, BorderLayout.PAGE_START);
        

        JScrollPane scrollPane = new JScrollPane(tableModel.getTable());

        sourcePanel.add(scrollPane, BorderLayout.CENTER);
        
        return sourcePanel;
    }

    @Override
    public void stop() {
        sourceTable.stop();
        targetTable.stop();
    }
}
