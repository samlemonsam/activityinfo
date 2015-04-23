package org.activityinfo.geoadmin.merge;

import org.activityinfo.geoadmin.ImportSource;
import org.activityinfo.geoadmin.merge.model.MatchRow;
import org.activityinfo.geoadmin.merge.model.MergeForm;
import org.activityinfo.geoadmin.merge.model.MergeModel;
import org.activityinfo.geoadmin.merge.table.ColumnAccessor;
import org.activityinfo.geoadmin.merge.table.MergeTableModel;
import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.geoadmin.model.AdminLevel;
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
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

/**
 * Window proving a user interface to match a shapefile to an existing admin
 * level. For example, updating with better/new geography or new entities.
 * 
 * <p>
 * The user, with a lot of help from automatic algorithms, needs to match each
 * feature from the shapefile to an existing admin entity.
 * 
 */
public class UpdateWindow extends JFrame {

    private AdminLevel level;
    private ActivityInfoClient client;
    private final MergeModel mergeModel;

    private final MergeTableModel tableModel;
    private final JTable table;

    public UpdateWindow(JFrame parent, ImportSource source, AdminLevel level, ActivityInfoClient client) throws IOException {
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
        final JButton mergeButton = new JButton("Match");
        mergeButton.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                mergeButton.setEnabled(isSelectionMergeable());
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
        toolbar.add(mergeButton);
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
    
    
//    /**
//     * Checks to see if the current selection is candidate for merging.
//     */
//    private boolean isSelectionMergeable() {
//        if (treeTable.getSelectedRowCount() != 2) {
//            return false;
//        }
//
//        MergeNode a = (MergeNode) treeTable.getPathForRow(
//            treeTable.getSelectedRows()[0]).getLastPathComponent();
//        MergeNode b = (MergeNode) treeTable.getPathForRow(
//            treeTable.getSelectedRows()[1]).getLastPathComponent();
//        if (a.isJoined() || b.isJoined()) {
//            return false;
//        }
//        return ((a.getFeature() == null && b.getFeature() != null) || (b.getFeature() == null && a.getFeature() != null));
//    }

    private void acceptTheirs() {
//        for (MergeNode node : getLeaves()) {
//            if(node.isLeaf()) {
//                if(node.getFeature() == null) {
//                    treeModel.setValueAt(MergeAction.DELETE, node, MergeTreeTableModel.ACTION_COLUMN);
//                } else if (node.getEntity() == null) {
//                    treeModel.setValueAt(MergeAction.UPDATE, node, MergeTreeTableModel.ACTION_COLUMN);
//                }
//            }
//        }
    }

    /**
     * Merges an unmatched existing entity with an unmatched imported feature
     */
//    private void mergeSelection() {
//        MergeNode a = (MergeNode) treeTable.getPathForRow(
//            treeTable.getSelectedRows()[0]).getLastPathComponent();
//        MergeNode b = (MergeNode) treeTable.getPathForRow(
//            treeTable.getSelectedRows()[1]).getLastPathComponent();
//
//        MergeNode entityNode;
//        MergeNode featureNode;
//
//        if (a.getEntity() != null) {
//            entityNode = a;
//            featureNode = b;
//        } else {
//            entityNode = b;
//            featureNode = a;
//        }
//
//        entityNode.setFeature(b.getFeature());
//
//        treeModel.fireNodeChanged(entityNode);
//        treeModel.removeNodeFromParent(featureNode);
//    }

    /**
     * Updates the server with the imported features.
     */
    private void update() {
//
//        List<AdminEntity> entities = Lists.newArrayList();
//        	
//        for (MergeNode join : getLeaves()) {
//            if (join.getAction() != null && join.getAction() != MergeAction.IGNORE) {
//                AdminEntity unit = new AdminEntity();
//                if (join.getEntity() != null) {
//                    unit.setId(join.getEntity().getId());
//                }
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
            return !mergeModel.getMatch(selectedRows[0]).isMerged() &&
                   !mergeModel.getMatch(selectedRows[1]).isMerged();
        } else {
            return false;
        }
    }
}
