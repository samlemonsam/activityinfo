package org.activityinfo.geoadmin.merge;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.geoadmin.GeoUtils;
import org.activityinfo.geoadmin.ImportSource;
import org.activityinfo.geoadmin.merge.model.MatchRow;
import org.activityinfo.geoadmin.merge.model.MergeForm;
import org.activityinfo.geoadmin.merge.model.MergeModel;
import org.activityinfo.geoadmin.merge.table.ColumnAccessor;
import org.activityinfo.geoadmin.merge.table.MergeTableModel;
import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.geoadmin.model.AdminEntity;
import org.activityinfo.geoadmin.model.AdminLevel;
import org.activityinfo.geoadmin.model.VersionMetadata;
import org.activityinfo.geoadmin.source.FeatureSourceCatalog;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;

/**
 * Window proving a user interface to match a shapefile to an existing admin
 * level. For example, updating with better/new geography or new entities.
 * 
 * <p>
 * The user, with a lot of help from automatic algorithms, needs to match each
 * feature from the shapefile to an existing admin entity.
 * 
 */
public class MergeWindow extends JFrame {

    private AdminLevel level;
    private ActivityInfoClient client;
    private final MergeModel mergeModel;

    private final MergeTableModel tableModel;
    private final JTable table;

    public MergeWindow(JFrame parent, ImportSource source, AdminLevel level, ActivityInfoClient client) throws IOException {
        super("Update " + level.getName());
        setSize(650, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.client = client;
        this.level = level;

        MergeForm targetForm = new MergeForm();
        targetForm.build(client, CuidAdapter.adminLevelFormClass(level.getId()));
        
        ResourceId importId = ResourceId.generateId();
        FeatureSourceCatalog catalog = new FeatureSourceCatalog();
        catalog.add(importId, source.getFile().getAbsolutePath());

        MergeForm sourceForm = new MergeForm();
        sourceForm.build(catalog, importId);

        mergeModel = new MergeModel(targetForm, sourceForm);
        mergeModel.build();
        
        tableModel = new MergeTableModel(mergeModel);
        table = new JTable(tableModel);
        
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);
        
        for(int i=0;i<tableModel.getColumnCount();++i) {
            ColumnAccessor columnModel = tableModel.getColumnAccessor(i);
            TableColumn tableColumn = table.getColumnModel().getColumn(i);
            tableColumn.setCellRenderer(columnModel.getCellRenderer());
            tableColumn.setResizable(columnModel.isResizable());
            if(!columnModel.isResizable()) {
                tableColumn.setWidth(columnModel.getWidth());
                tableColumn.setMaxWidth(columnModel.getWidth());
                tableColumn.setPreferredWidth(columnModel.getWidth());
            }
        }

        getContentPane().add(createToolbar(), BorderLayout.PAGE_START);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JToolBar createToolbar() {
        final JButton matchButton = new JButton("Match");
        matchButton.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                matchButton.setEnabled(isSelectionMergeable());
            }
        });
        matchButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                matchSelection();
            }
        });
        
        final JButton unMergeButton = new JButton("Unmatch");
        unMergeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unMatchSelection();
            }
        });


        final JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSelectionDeleted(true);
            }
        });
        
        final JButton restoreButton = new JButton("Undelete");
        restoreButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSelectionDeleted(false);
            }
        });

     
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                update();
            }
        });

        JToolBar toolbar = new JToolBar();
        toolbar.add(matchButton);
        toolbar.add(unMergeButton);
        toolbar.addSeparator();
        toolbar.add(deleteButton);
        toolbar.add(restoreButton);
        toolbar.addSeparator();
        toolbar.add(updateButton);
        return toolbar;
    }



    private void toggleSelectionDeleted(boolean deleted) {
        ListSelectionModel selection = table.getSelectionModel();
        
        for(int i=selection.getMinSelectionIndex();i<=selection.getMaxSelectionIndex();++i) {
            if(selection.isSelectedIndex(i)) {
                MatchRow match = mergeModel.getMatch(table.convertRowIndexToModel(i));
                match.setDeleted(deleted);
            }
        }

        tableModel.fireTableRowsUpdated(selection.getMinSelectionIndex(), selection.getMaxSelectionIndex());
    }

    private void unMatchSelection() {
        ListSelectionModel selection = table.getSelectionModel();
        
        // first make a list of selected matches as their indices may change

        Set<MatchRow> selected = new HashSet<>();
        for(int i=selection.getMinSelectionIndex();i<=selection.getMaxSelectionIndex();++i) {
            if(selection.isSelectedIndex(i)) {
                selected.add(mergeModel.getMatch(table.convertRowIndexToModel(i)));
            }
        }

        ListIterator<MatchRow> it = mergeModel.getMatches().listIterator();
        while(it.hasNext()) {
            MatchRow match = it.next();
            if(selected.contains(match)) {
                MatchRow newRow = match.split();
                it.add(newRow);
            }
        }
        
        tableModel.fireTableDataChanged();
        
    }

    /**
     * Updates the server with the imported features.
     */
    private void update() {

//        java.util.List<AdminEntity> entities = Lists.newArrayList();
//        	
//        for (MatchRow match : mergeModel.getMatches()) {
//            AdminEntity unit = new AdminEntity();
//            if(match.isTargetMatched() && match.isDeleted()) {
//                unit.setId(CuidAdapter.getLegacyIdFromCuid(mergeModel.getTargetId(match)));
//                unit.setDeleted(true);
//            
//            } else if(!match.isDeleted()) {
//                if(match.isTargetMatched()) {
//                    unit.setId(CuidAdapter.getLegacyIdFromCuid(mergeModel.getTargetId(match)));
//                }
//                unit.setName();
//            
//            if (join.getEntity() != null) {
//                unit.setId(join.getEntity().getId());
//            }
//                if (join.getFeature() != null) {
//                    unit.setName(join.getFeature().getAttributeStringValue(form.getNameProperty()));
//                    if (form.getCodeProperty() != null) {
//                        unit.setCode(join.getFeature().getAttributeStringValue(form.getCodeProperty()));
//                    }
//                    unit.setBounds(GeoUtils.toBounds(join.getFeature().getEnvelope()));
//                    unit.setGeometry(join.getFeature().getGeometry());
//                }
//                unit.setDeleted(join.getAction() == MergeAction.DELETE);
//                entities.add(unit);
//            }
//        }
//
//        VersionMetadata metadata = new VersionMetadata();
//        metadata.setSourceFilename(source.getFile().getName());
//        metadata.setSourceMD5(source.getMd5Hash());
//        metadata.setSourceUrl(form.getSourceUrl());
//        metadata.setMessage(form.getMessage());
//        try {
//            metadata.setSourceMetadata(source.getMetadata());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        
//        AdminLevel updatedLevel = new AdminLevel();
//        updatedLevel.setId(level.getId());
//        updatedLevel.setName(level.getName());
//        updatedLevel.setParentId(level.getParentId());
//        updatedLevel.setEntities(entities);
//        updatedLevel.setVersionMetadata(metadata);
//
//
//        client.updateAdminLevel(updatedLevel);
//        	
//        setVisible(false);
    }


    public boolean isSelectionMergeable() {
        if (table.getSelectedRowCount() == 2) {
            int selectedRows[] = table.getSelectedRows();
            MatchRow a = mergeModel.getMatch(selectedRows[0]);
            MatchRow b = mergeModel.getMatch(selectedRows[1]);
            
            return a.canMatch(b);
        } else {
            return false;
        }
    }


    private void matchSelection() {
        Preconditions.checkState(isSelectionMergeable());

        int[] selectedRows = table.getSelectedRows();
        
        mergeModel.match(selectedRows[0], selectedRows[1]);
        
        tableModel.fireTableRowsUpdated(selectedRows[0], selectedRows[0]);
        tableModel.fireTableRowsDeleted(selectedRows[1], selectedRows[1]);
    }
}
