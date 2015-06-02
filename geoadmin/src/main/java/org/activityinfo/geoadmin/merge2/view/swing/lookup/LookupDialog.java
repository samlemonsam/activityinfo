package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import org.activityinfo.geoadmin.merge2.view.mapping.LookupTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Arrays;

/**
 * Dialog that allows a user to choose from among close matches
 */
public class LookupDialog extends JDialog {

    private final LookupDialogTableModel tableModel;
    private final JTable table;

    public LookupDialog(JPanel parent, LookupTable lookupTable, int sourceKeyIndex) {
        super(SwingUtilities.getWindowAncestor(parent), "Lookup", ModalityType.APPLICATION_MODAL);
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        tableModel = new LookupDialogTableModel(lookupTable, sourceKeyIndex);

        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        add(new JScrollPane(table), BorderLayout.CENTER);
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
