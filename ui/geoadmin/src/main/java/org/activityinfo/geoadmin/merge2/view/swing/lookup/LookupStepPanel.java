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
        table = mapping.getLookupTable();

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
