package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.match.MatchRow;
import org.activityinfo.geoadmin.merge2.view.match.MatchSide;
import org.activityinfo.geoadmin.merge2.view.match.MatchTable;
import org.activityinfo.geoadmin.merge2.view.swing.match.MatchStepPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Allows the user to choose 
 */
public class SelectDialog extends JDialog {


    private final JTable candidateTable;
    private final JTable headerTable;

    public SelectDialog(MatchStepPanel parent, ImportView viewModel, 
                        final int matchRowIndex, 
                        final MatchSide side) {
        
        super(SwingUtilities.getWindowAncestor(parent), "Choose", ModalityType.APPLICATION_MODAL);
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        
        JPanel toolBar = new JPanel();
        toolBar.setLayout(new FlowLayout());
        
        MatchTable matchTable = viewModel.getMatchTable();
        final MatchRow matchRow = matchTable.get(matchRowIndex);
        final KeyFieldPairSet keyFields = matchTable.getKeyFields();
        
        /*
         * The header table shows the single row that were are looking to match against
         */
        AbstractTableModel headerModel = new HeaderTableModel(keyFields, matchRow, side);

        /**
         * THe rest of of the model shows 
         */
        
        AbstractTableModel candidateModel = new CandidateTableModel(matchTable.getGraph().get(), matchRow, side);

        headerTable = new JTable( headerModel );
   //     headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        headerTable.setRowSelectionAllowed(false);
        headerTable.setCellSelectionEnabled(false);
     

        candidateTable = new JTable(candidateModel);
        candidateTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        candidateTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        headerTable.getColumnModel().addColumnModelListener(new SyncingColumnListener(headerTable, candidateTable));
        
        JScrollPane headerScroll = new JScrollPane(headerTable);
        headerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        headerScroll.setVerticalScrollBar(new HiddenScrollBar(headerScroll.getVerticalScrollBar()));

        JScrollPane bodyScroll = new JScrollPane(candidateTable) {
            public void setColumnHeaderView(Component view) {} // work around
        };
        headerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        bodyScroll.getHorizontalScrollBar().addAdjustmentListener(
                new SyncingAdjustmentListener(headerScroll.getHorizontalScrollBar()));

        bodyScroll.setPreferredSize(new Dimension(400, 100));
        headerScroll.setPreferredSize(new Dimension(400, 40));  // Hmm...
        
        getContentPane().add(headerScroll, BorderLayout.NORTH);
        getContentPane().add(bodyScroll, BorderLayout.CENTER);


        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JButton changeButton = new JButton("OK");
        changeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(cancelButton);
        buttonPanel.add(changeButton);

        add(buttonPanel, BorderLayout.PAGE_END);
    }

}
