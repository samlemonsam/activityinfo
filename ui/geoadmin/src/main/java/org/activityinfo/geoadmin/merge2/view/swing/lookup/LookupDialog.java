package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.model.ReferenceMatch;
import org.activityinfo.geoadmin.merge2.view.mapping.LookupTable;
import org.activityinfo.geoadmin.merge2.view.mapping.SourceLookupKey;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialog that allows a user to choose/override the look up value.
 */
public class LookupDialog extends JDialog {

    private final LookupDialogTableModel tableModel;
    private final JTable table;
    private LookupTable lookupTable;
    private SourceLookupKey sourceKey;
    private ImportModel model;

    public LookupDialog(JPanel parent, ImportModel model,  LookupTable lookupTable, int sourceKeyIndex) {
        super(SwingUtilities.getWindowAncestor(parent), "Lookup", ModalityType.APPLICATION_MODAL);
        this.model = model;
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.lookupTable = lookupTable;
        this.sourceKey = lookupTable.getSourceKey(sourceKeyIndex);

        tableModel = new LookupDialogTableModel(lookupTable, sourceKeyIndex);

        table = new JTable(tableModel);
        
        // Sort the table by score, descending, so the best match is at the top
        table.setAutoCreateRowSorter(true);
        table.getRowSorter().setSortKeys(Arrays.asList(
                new RowSorter.SortKey(LookupDialogTableModel.SCORE_COLUMN, SortOrder.DESCENDING)));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    int rowIndex = table.rowAtPoint(e.getPoint());
                    if(rowIndex != -1) {
                        int candidateIndex = table.convertRowIndexToModel(rowIndex);
                        if(candidateIndex != -1) {
                            changeMatch(candidateIndex);
                        }
                    }
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

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
                changeMatch(table.getSelectedRow());
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(cancelButton);
        buttonPanel.add(changeButton);
        
        add(buttonPanel, BorderLayout.PAGE_END);
    }
    
    private void changeMatch(int candidateIndex) {
        
        // Update our import model
        model.getReferenceMatches().add(buildReferenceMatch(candidateIndex));
        
        
        // Close the form
        setVisible(false);
    }

    /**
     * Build a ReferenceMatch that can be stored as part of the model.
     * since this will form part of the model "state" it cannot depend 
     * on computed values in any way, so we need to map our row indices and SourceKeys
     * back to the original ids.
     *
     * @param candidateIndex the index within the list of candidate target matches (from LookupGraph)
     */
    private ReferenceMatch buildReferenceMatch(int candidateIndex) {
        ResourceId targetId = tableModel.getTargetInstanceId(candidateIndex);

        Map<FieldPath, String> keyMap = new HashMap<>();
        java.util.List<FieldProfile> keyFields = lookupTable.getSourceKeyFields();
        for (int i = 0; i < keyFields.size(); i++) {
            keyMap.put(keyFields.get(i).getPath(), sourceKey.get(i));
        }

        return new ReferenceMatch(keyMap, targetId);
    }

    public void setSelection(int targetRowIndex) {
        if(targetRowIndex != -1) {
            int modelRow = tableModel.convertTableIndexToModel(targetRowIndex);
            if (modelRow != -1) {
                int viewRow = table.convertRowIndexToView(modelRow);
                table.setRowSelectionInterval(viewRow, viewRow);
            }
        }
    }
    
    private class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                                                       boolean hasFocus, int row, int column) {
            
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(!isSelected) {
                
                
            }
            return cell;
        }
    }
}
