package org.activityinfo.geoadmin;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.activityinfo.geoadmin.model.AdminEntity;
import org.activityinfo.geoadmin.model.AdminLevel;
import org.activityinfo.geoadmin.model.ActivityInfoClient;

/**
 * Modal window that simply lists the admin entities currently on the server.
 */
public class AdminListWindow extends JDialog {

    private AdminTableModel tableModel;

    public AdminListWindow(JFrame parent, ActivityInfoClient client, AdminLevel level) {
        super(parent, level.getName(), Dialog.ModalityType.APPLICATION_MODAL);
        setSize(650, 350);
        setLocationRelativeTo(parent);

        List<AdminEntity> adminEntities = client.getAdminEntities(level);

        setTitle(level.getName() + " [" + adminEntities.size() + " entities]");

        tableModel = new AdminTableModel(adminEntities);

        addTable();
        addToolBar();

    }

    private void addTable() {
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);

        JScrollPane scroll = new JScrollPane(table);

        getContentPane().add(scroll);
    }

    private void addToolBar() {

        final JButton exportButton = new JButton("Export");
        exportButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TableExporter.export(tableModel, exportButton);
            }
        });

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(exportButton);
        toolBar.addSeparator();

        getContentPane().add(toolBar, BorderLayout.PAGE_START);

    }
}
