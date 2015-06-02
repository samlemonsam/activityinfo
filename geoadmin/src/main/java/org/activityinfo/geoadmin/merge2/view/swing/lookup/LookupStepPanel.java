package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.mapping.LookupTable;
import org.activityinfo.geoadmin.merge2.view.mapping.ReferenceFieldMapping;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;
import org.activityinfo.observable.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Provides the user interface for the user to validate the lookup of reference
 * field values
 */
public class LookupStepPanel extends StepPanel {

    private final Observable<LookupTable> table;
    private ImportView viewModel;
    private final LookupTableModel tableModel;

    public LookupStepPanel(ImportView viewModel, ReferenceFieldMapping mapping) {
        this.viewModel = viewModel;
        table = LookupTable.compute(mapping, viewModel);

        tableModel = new LookupTableModel(mapping, table);
        final JTable tableComponent = new JTable(tableModel);
        
        tableComponent.setDefaultRenderer(Object.class, new LookupCellRenderer(mapping.getLookupGraph(), table));
        tableComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int sourceKeyIndex = tableComponent.rowAtPoint(e.getPoint());
                    if (sourceKeyIndex != -1) {
                        choose(sourceKeyIndex);
                    }
                }
            }
        });
        setLayout(new BorderLayout());
        
        add(new JScrollPane(tableComponent), BorderLayout.CENTER);
    }

    private void choose(int sourceKeyIndex) {
        LookupDialog dialog = new LookupDialog(this, viewModel.getModel(), table.get(), sourceKeyIndex);
        dialog.setSelection(table.get().getTargetMatchRow(sourceKeyIndex));
        dialog.setVisible(true);
    }


    @Override
    public void stop() {
        tableModel.stop();
    }
}
