package org.activityinfo.geoadmin;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.activityinfo.geoadmin.locations.LocationWindow;
import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.swing.ImportDialog;
import org.activityinfo.geoadmin.model.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.ResourceStoreImpl;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Principal tab which lists the countries present in ActivityInfo
 */
public class CountryTab extends JPanel {

    private Preferences prefs = Preferences.userNodeForPackage(GeoAdmin.class);

    private Country country;
    private ActivityInfoClient client;
    private JTree tree;

    public CountryTab(ActivityInfoClient client, Country country) {
        super(false); // isDoubleBuffered
        this.client = client;
        this.country = country;

        setLayout(new GridLayout(1, 1));

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(country.getName());
        rootNode.add(createAdminNodes());
        rootNode.add(createLocationTypeNodes());
        
        tree = new JTree(rootNode);
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selPath != null) {

                    if (SwingUtilities.isRightMouseButton(e)) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        if (node.getUserObject() instanceof AdminLevel) {
                            AdminLevel level = (AdminLevel) node.getUserObject();
                            showAdminLevelContextMenu(e, level);
                        } else {
                            showRootContextMenu(e);
                        }

                    } else if (e.getClickCount() == 2) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        if (node.getUserObject() instanceof AdminLevel) {
                            showAdminLevels(e, (AdminLevel) node.getUserObject());
                        } else if(node.getUserObject() instanceof LocationType) {
                        	showLocationType(e, (LocationType) node.getUserObject());
                        }
                    }
                }
            }
        });
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    private void showAdminLevels(MouseEvent e, AdminLevel level) {
        AdminListWindow window = new AdminListWindow(getParentFrame(), client, level);
        window.setVisible(true);
    }
    


	private void showLocationType(MouseEvent e, LocationType locationType) {
		LocationWindow window = new LocationWindow(getParentFrame(), country, locationType, client);
		window.setVisible(true);
	}

    private MutableTreeNode createAdminNodes() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Admin Levels");
        List<AdminLevel> levels = client.getAdminLevels(country);

        Map<Integer, DefaultMutableTreeNode> nodes = Maps.newHashMap();

        // add root nodes
        for (AdminLevel level : levels) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(level);
            nodes.put(level.getId(), node);
            if (level.isRoot()) {
                rootNode.add(node);
            }
        }

        // add child nodes
        for (AdminLevel level : levels) {
            if (!level.isRoot()) {
                DefaultMutableTreeNode parent = nodes.get(level.getParentId());
                DefaultMutableTreeNode node = nodes.get(level.getId());
                parent.add(node);
            }
        }
        return rootNode;
    }
    
    private MutableTreeNode createLocationTypeNodes() {
    	DefaultMutableTreeNode typeNodes = new DefaultMutableTreeNode("Location Types");
    	List<LocationType> types = client.getLocationTypesByCountryCode(country.getCode());

        // add child nodes
        for (LocationType type : types) {
        	typeNodes.add(new DefaultMutableTreeNode(type));
        }
        
        return typeNodes;
    }

    private void showAdminLevelContextMenu(MouseEvent e, final AdminLevel level) {

        JMenuItem renameItem = new JMenuItem("Rename level");
        renameItem.addActionListener(new ActionListener() {

            @Override		
            public void actionPerformed(ActionEvent e) {
                renameLevel(level);
            }
        });

        JMenuItem importChildItem = new JMenuItem("Import child level");
        importChildItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                importChildLevel(level);
            }
        });
        
        JMenuItem updateItem = new JMenuItem("Update from file");
        updateItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateLevel(level);
            }
        });

        JPopupMenu menu = new JPopupMenu();
        menu.add(renameItem);
        menu.add(importChildItem);
        menu.add(updateItem);

        menu.show(e.getComponent(), e.getX(), e.getY());
    }


	private void showRootContextMenu(MouseEvent e) {

        JMenuItem importChildItem = new JMenuItem("Import new top level");
        importChildItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                importChildLevel(null);
            }
        });

        JPopupMenu menu = new JPopupMenu();
        menu.add(importChildItem);

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void renameLevel(AdminLevel level) {
        String newName = JOptionPane.showInputDialog(getParentFrame(), "New name");
        if (!Strings.isNullOrEmpty(newName)) {

            AdminLevel update = new AdminLevel();
            update.setId(level.getId());
            update.setName(newName);
            update.setParentId(level.getParentId());

            VersionMetadata metadata = new VersionMetadata();
            metadata.setMessage(String.format("Renamed level from '%s' to '%s'",
                level.getName(), newName));
            update.setVersionMetadata(metadata);

            client.updateAdminLevel(update);

            level.setName(newName);

        }
    }

    private void importChildLevel(AdminLevel level) {
        File file = chooseFile();
        if (file != null) {
            try {
                ImportWindow window = new ImportWindow(getParentFrame(), client, country, level, file);
                window.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateLevel(AdminLevel level) {
        ImportSource source = chooseSource();
        if (source != null) {
            try {
                ResourceId targetId = CuidAdapter.adminLevelFormClass(level.getId());
                ResourceId sourceId = ResourceId.valueOf("file://" + source.getFile().getAbsolutePath());
                ImportModel model = new ImportModel(sourceId, targetId);
                ImportView viewModel = new ImportView(new ResourceStoreImpl(client), model);
                ImportDialog dialog = new ImportDialog(viewModel);
                dialog.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private JFrame getParentFrame() {
        JFrame frame = (JFrame) SwingUtilities.getRoot(this);
        return frame;
    }

    private ImportSource chooseSource() {
        File file = chooseFile();
        if (file == null) {
            return null;
        }
        try {
            ImportSource source = new ImportSource(file);
            if (source.getFeatureCount() == 0) {
                JOptionPane.showMessageDialog(getParentFrame(),
                    "The file is empty.", file.getName(),
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!source.validateGeometry(country)) {
                JOptionPane.showMessageDialog(getParentFrame(),
                    "The geometry does not seem to match this country's geographic bounds, please " +
                        "ensure that the .prj file correctly defines the projection.",
                    file.getName(), JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return source;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(getParentFrame(),
                "There was an exception opening this source:  " + e.getMessage(),
                file.getName(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }

    private File chooseFile() {
        File initialDir = new File(prefs.get("import_dir_" + country.getCode(),
            prefs.get("import_dir", "")));

        JFileChooser chooser = new JFileChooser(initialDir);
        chooser.setFileFilter(new FileNameExtensionFilter("Shapefiles", "shp"));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            prefs.put("import_dir_" + country.getCode(), file.getParent());
            prefs.put("import_dir", file.getParent());
            return file;
        }
        return null;
    }
}
