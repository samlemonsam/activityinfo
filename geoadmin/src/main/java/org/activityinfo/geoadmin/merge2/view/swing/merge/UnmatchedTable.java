package org.activityinfo.geoadmin.merge2.view.swing.merge;

import com.google.common.collect.BiMap;
import org.activityinfo.geoadmin.merge2.view.model.FormMapping;
import org.activityinfo.geoadmin.merge2.view.model.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.model.SourceFieldMapping;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.observable.Subscription;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;


public class UnmatchedTable {
    public static final int COLUMN_WIDTH = 50;
    private final JTable table;
    private final UnmatchedTableModel tableModel;
    private final DefaultTableColumnModel tableColumnModel;
    
    private final Subscription subscription;
    private final MergeSide side;
    private final Observable<FormMapping> fieldMapping;

    private boolean firing = false;
    private boolean draggingColumns = false;

    public UnmatchedTable(MergeSide side, final Observable<FormMapping> fieldMapping) {
        this.side = side;
        this.fieldMapping = fieldMapping;
        this.tableModel = new UnmatchedTableModel();
        this.tableColumnModel = new DefaultTableColumnModel();
        this.table = new JTable(tableModel, tableColumnModel);
        this.table.getTableHeader().setReorderingAllowed(false);
        
        subscription = this.fieldMapping.subscribe(new Observer<FormMapping>() {
            @Override
            public void onChange(Observable<FormMapping> observable) {
                if(fieldMapping.isLoading()) {
                    return;
                }
                updateTable();
            }
        });
    }
    
    public String getHeader() {
        switch (side) {
            case TARGET:
                return "Target";
            case SOURCE:
                return "Source";
        }
        throw new IllegalStateException();
    }

    private void updateTable() {
        updateColumns();
    }


    private void updateColumns() {
        BiMap<FieldProfile, FieldProfile> map;

        // First add target fields
        int modelIndex = 0;

        List<FieldProfile> fields = new ArrayList<>();

        map = fieldMapping.get().asMap(MergeSide.TARGET, side);
        for (FieldProfile targetField : fieldMapping.get().getTarget().getFields()) {
            fields.add(map.get(targetField));
        }

        map = fieldMapping.get().asMap(MergeSide.SOURCE, side);
        for (SourceFieldMapping sourceField : fieldMapping.get().getMappings()) {
            if(!sourceField.getTargetField().isPresent()) {
                fields.add(map.get(sourceField.getSourceField()));
            }
        }
        
        for(FieldProfile field : fields) {
            TableColumn column = new TableColumn(modelIndex++, COLUMN_WIDTH);
            column.setPreferredWidth(COLUMN_WIDTH);
            if(field == null) {
                column.setHeaderValue("");
            } else {
                column.setHeaderValue(field.getLabel());
            }
            tableColumnModel.addColumn(column);
        }
        
        tableModel.update(fields, fieldMapping.get().getProfile(side).getRowCount());
    }


    public JTable getTable() {
        return table;
    }

    public void stop() {
        subscription.unsubscribe();
    }
    
    public void syncColumnsWith(final UnmatchedTable other) {
        table.getTableHeader().addMouseListener(new MouseInputAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                draggingColumns = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingColumns = false;
                updateColumnWidths(other);
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                if (me.getClickCount() == 2) {
                    // your valueChanged overridden method 
                }
            }
        });    
    }

    private void updateColumnWidths(UnmatchedTable other) {
        for(int i=0;i!=tableColumnModel.getColumnCount();++i) {
            TableColumn thisColumn = tableColumnModel.getColumn(i);
            TableColumn thatColumn = other.tableColumnModel.getColumn(i);
            
            if(thatColumn.getWidth() != thisColumn.getWidth()) {
                thatColumn.setPreferredWidth(thisColumn.getWidth());
            }
        }
    }
}
