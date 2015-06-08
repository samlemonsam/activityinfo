package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import org.activityinfo.geoadmin.merge2.model.InstanceMatch;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.match.MatchRow;
import org.activityinfo.geoadmin.merge2.view.match.MatchSide;
import org.activityinfo.geoadmin.merge2.view.match.MatchTable;
import org.activityinfo.geoadmin.merge2.view.swing.match.MatchStepPanel;
import org.activityinfo.model.resource.ResourceId;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Allows the user to choose 
 */
public class SelectDialog extends JDialog {


    private final JTable candidateTable;
    private final JTable headerTable;
    private final ImportView viewModel;
    
    private MatchSide fromSide;
    private int fromIndex;
    private final KeyFieldPairSet keyFields;

    public SelectDialog(MatchStepPanel parent, ImportView viewModel, 
                        final int matchRowIndex, 
                        final MatchSide fromSide) {
        
        super(SwingUtilities.getWindowAncestor(parent), "Choose", ModalityType.APPLICATION_MODAL);
        this.viewModel = viewModel;
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        
        JPanel toolBar = new JPanel();
        toolBar.setLayout(new FlowLayout());
        
        MatchTable matchTable = viewModel.getMatchTable();
        final MatchRow matchRow = matchTable.get(matchRowIndex);
        keyFields = matchTable.getKeyFields();

        this.fromSide = fromSide;
        this.fromIndex = matchRow.getRow(fromSide);
        
        /*
         * The header table shows the single row that were are looking to match against
         */
        HeaderTableModel headerModel = new HeaderTableModel(keyFields, matchRow, fromSide);

        /**
         * THe rest of of the model shows 
         */
        
        final CandidateTableModel candidateModel = new CandidateTableModel(matchTable.getGraph().get(), fromIndex, fromSide);

        headerTable = new JTable( headerModel );
        headerTable.setRowSelectionAllowed(false);
        headerTable.setCellSelectionEnabled(false);
     

        candidateTable = new JTable(candidateModel);
        candidateTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        candidateTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        candidateTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    int viewRow = candidateTable.getSelectedRow();
                    int candidateIndex = candidateTable.convertRowIndexToModel(viewRow);
                    int targetIndex = candidateModel.candidateRowToInstanceIndex(candidateIndex);
                    
                    updateMatch(targetIndex);
                }
            }
        });

        int selectInstanceIndex = matchRow.getRow(fromSide.opposite());
        if(selectInstanceIndex != -1) {
            int selectedCandidateIndex = candidateModel.instanceIndexToCandidateRow(selectInstanceIndex);
            int selectedViewRow = candidateTable.convertRowIndexToView(selectedCandidateIndex);
            candidateTable.setRowSelectionInterval(selectedViewRow, selectedViewRow);
        }
        
        headerTable.getColumnModel().addColumnModelListener(new SyncingColumnListener(headerTable, candidateTable));
        
        JScrollPane headerScroll = new JScrollPane(headerTable);
        headerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        headerScroll.setVerticalScrollBar(new HiddenScrollBar(headerScroll.getVerticalScrollBar()));

        JScrollPane candidateScroll = new JScrollPane(candidateTable) {
            public void setColumnHeaderView(Component view) {} // work around
        };
        headerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        candidateScroll.getHorizontalScrollBar().addAdjustmentListener(
                new SyncingAdjustmentListener(headerScroll.getHorizontalScrollBar()));

        candidateScroll.setPreferredSize(new Dimension(400, 100));
        headerScroll.setPreferredSize(new Dimension(400, 40));  // Hmm...
        
        getContentPane().add(headerScroll, BorderLayout.NORTH);
        getContentPane().add(candidateScroll, BorderLayout.CENTER);


        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
        JButton changeButton = new JButton("Match");
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

   
    private void updateMatch(int matchedIndex) {

        ResourceId fromId = keyFields.getForm(fromSide).getRowId(fromIndex);
        ResourceId toId = keyFields.getForm(fromSide.opposite()).getRowId(matchedIndex);
        
        InstanceMatch newMatch;
        if(fromSide == MatchSide.SOURCE) {
            newMatch = new InstanceMatch(fromId, toId);
        } else {
            newMatch = new InstanceMatch(toId, fromId);
        }
        
        viewModel.getModel().getInstanceMatchSet().add(newMatch);
        
        setVisible(false);
    }


}
