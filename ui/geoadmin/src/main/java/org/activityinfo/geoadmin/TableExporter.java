package org.activityinfo.geoadmin;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

/**
 * Exports the contents of a JTable to a CSV file
 * 
 */
public class TableExporter {

    private static Preferences prefs = Preferences
        .userNodeForPackage(TableExporter.class);

    public static void export(TableModel table, JComponent owner) {
        File file = chooseFile(owner);
        if (file != null) {
            PrintWriter writer;
            try {
                writer = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            writeTable(table, writer);
            writer.close();
        }

    }

    private static void writeTable(TableModel table, PrintWriter writer) {
        // write headers
        for (int column = 0; column != table.getColumnCount(); ++column) {
            if (column > 0) {
                writer.print("\t");
            }
            writer.print(table.getColumnName(column));
        }
        writer.println();

        // write data
        for (int row = 0; row != table.getRowCount(); ++row) {
            for (int column = 0; column != table.getColumnCount(); ++column) {
                if (column > 0) {
                    writer.print("\t");
                }
                writer.print(table.getValueAt(row, column));
            }
            writer.println();
        }
        writer.flush();
    }

    private static File chooseFile(Component owner) {
        File initialDir = new File(prefs.get("export_dir", ""));

        JFileChooser chooser = new JFileChooser(initialDir);
        chooser.setFileFilter(new FileNameExtensionFilter(
            "Tab seperated values", "tab"));
        int returnVal = chooser.showSaveDialog(owner);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            prefs.put("export_dir", file.getParent());
            return file;
        }
        return null;
    }
}
