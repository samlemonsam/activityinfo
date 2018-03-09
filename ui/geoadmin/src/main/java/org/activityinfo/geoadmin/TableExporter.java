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
package org.activityinfo.geoadmin;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

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
