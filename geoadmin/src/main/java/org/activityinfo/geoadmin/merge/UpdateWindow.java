package org.activityinfo.geoadmin.merge;

import com.google.common.collect.Lists;
import net.miginfocom.swing.MigLayout;
import org.activityinfo.geoadmin.*;
import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.geoadmin.model.AdminEntity;
import org.activityinfo.geoadmin.model.AdminLevel;
import org.activityinfo.geoadmin.model.VersionMetadata;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

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

    private List<Join> joins;
    private ImportSource source;
    private UpdateForm form;
    private AdminLevel level;
    private ActivityInfoClient client;
    private JLabel scoreLabel;
    private MergeTableModel tableModel;
    private JTable table;

    public UpdateWindow(JFrame parent, ImportSource source, AdminLevel level, ActivityInfoClient client) {
        super("Update " + level.getName());
        setSize(650, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.client = client;
        this.level = level;
        this.source = source;

        form = new UpdateForm(source);

        FormTree targetTree = client.getFormTree(CuidAdapter.adminLevelFormClass(level.getId()));
        
        tableModel = new MergeTableModel(null);
        table = new JTable(tableModel);
        
        JComboBox actionCombo = new JComboBox(MergeAction.values());
        

        scoreLabel = new JLabel();
        JLabel countLabel = new JLabel(source.getFeatureCount() + " features");

        JPanel panel = new JPanel(new MigLayout("fill"));
        panel.add(form, "wrap");
        panel.add(new JScrollPane(table), "wrap,grow");

        panel.add(scoreLabel, "height 25!, wrap, growx");
        panel.add(countLabel);

        getContentPane().add(createToolbar(), BorderLayout.PAGE_START);
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    /**
     * Displays the score of the selected match bet
     * 
     * @param event
     */
    private void showScore(TreeSelectionEvent event) {
        MergeNode node = (MergeNode) event.getPath().getLastPathComponent();
        if (node.getFeature() == null || node.getEntity() == null) {
            scoreLabel.setText("");
        } else {
            double nameSim = node.getFeature().similarity(node.getEntity().getName());
            double intersection = Joiner.areaOfIntersection(node.getEntity(), node.getFeature());

            scoreLabel.setText(String.format("Name match: %.2f  Intersection: %.2f",
                nameSim, intersection));
        }
    }

    private JToolBar createToolbar() {

        final JButton acceptTheirsButton = new JButton("Accept Theirs");
        acceptTheirsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                acceptTheirs();
            }
        });

        final JButton mergeButton = new JButton("Merge");
        mergeButton.setEnabled(false);
//        treeTable.addTreeSelectionListener(new TreeSelectionListener() {
//
//            @Override
//            public void valueChanged(TreeSelectionEvent event) {
//                mergeButton.setEnabled(isSelectionMergeable());
//            }
//        });
//        mergeButton.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent arg0) {
//                mergeSelection();
//            }
//        });


        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                update();
            }
        });

        JToolBar toolbar = new JToolBar();
        toolbar.add(acceptTheirsButton);
        toolbar.add(mergeButton);
        toolbar.add(updateButton);

        return toolbar;
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

        List<AdminEntity> entities = Lists.newArrayList();
        	
        for (MergeNode join : getLeaves()) {
            if (join.getAction() != null && join.getAction() != MergeAction.IGNORE) {
                AdminEntity unit = new AdminEntity();
                if (join.getEntity() != null) {
                    unit.setId(join.getEntity().getId());
                }
                if (join.getFeature() != null) {
                    unit.setName(join.getFeature().getAttributeStringValue(form.getNameProperty()));
                    if (form.getCodeProperty() != null) {
                        unit.setCode(join.getFeature().getAttributeStringValue(form.getCodeProperty()));
                    }
                    unit.setBounds(GeoUtils.toBounds(join.getFeature().getEnvelope()));
                    unit.setGeometry(join.getFeature().getGeometry());
                }
                unit.setDeleted(join.getAction() == MergeAction.DELETE);
                entities.add(unit);
            }
        }

        VersionMetadata metadata = new VersionMetadata();
        metadata.setSourceFilename(source.getFile().getName());
        metadata.setSourceMD5(source.getMd5Hash());
        metadata.setSourceUrl(form.getSourceUrl());
        metadata.setMessage(form.getMessage());
        try {
            metadata.setSourceMetadata(source.getMetadata());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        AdminLevel updatedLevel = new AdminLevel();
        updatedLevel.setId(level.getId());
        updatedLevel.setName(level.getName());
        updatedLevel.setParentId(level.getParentId());
        updatedLevel.setEntities(entities);
        updatedLevel.setVersionMetadata(metadata);


        client.updateAdminLevel(updatedLevel);
        	
        setVisible(false);
    }

	private List<MergeNode> getLeaves() {
//        List<MergeNode> nodes = ((MergeNode) treeModel.getRoot()).getLeaves();
//        return nodes;
        throw new UnsupportedOperationException();
    }
}
