package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.mapping.ReferenceFieldMapping;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Provides the user interface for the user to validate the lookup of reference
 * field values
 */
public class LookupStepPanel extends StepPanel {

    private ImportView importModel;
    private ReferenceFieldMapping mapping;

    public LookupStepPanel(ImportView importModel, ReferenceFieldMapping mapping) {
        this.importModel = importModel;
        this.mapping = mapping;

        LookupTableModel tableModel = new LookupTableModel(mapping.getLookupTable());
        final JTable table = new JTable(tableModel);
        table.setDefaultRenderer(Object.class, new LookupCellRenderer(mapping.getLookupTable()));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int sourceKeyIndex = table.rowAtPoint(e.getPoint());
                    if(sourceKeyIndex != -1) {
                        choose(sourceKeyIndex);
                    }
                }
            }
        });
        setLayout(new BorderLayout());
        
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void choose(int sourceKeyIndex) {
        LookupDialog dialog = new LookupDialog(this, mapping.getLookupTable(), sourceKeyIndex);
        dialog.setSelection(mapping.getLookupTable().getTargetMatchRow(sourceKeyIndex));
        dialog.setVisible(true);
    }


    @Override
    public void stop() {
            
    }
}
