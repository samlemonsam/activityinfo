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
package org.activityinfo.geoadmin.writer;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.PrintWriter;

public class TableWriter {
    
    public static void export(TableModel tableModel) {
        try {
            File tempFile = File.createTempFile("match", ".csv");
            try (PrintWriter writer = new PrintWriter(tempFile)) {

                int numCols = tableModel.getColumnCount();

                for (int j = 0; j < numCols; ++j) {
                    if (j > 0) {
                        writer.print(",");
                    }
                    writer.print(tableModel.getColumnName(j));
                }
                writer.println();

                int numRows = tableModel.getRowCount();

                for (int i = 0; i < numRows; ++i) {
                    for (int j = 0; j < numCols; ++j) {
                        if (j > 0) {
                            writer.print(",");
                        }
                        Object value = tableModel.getValueAt(i, j);
                        if(value != null) {
                            String stringValue = value.toString();
                            if(stringValue.contains(",")) {
                                writer.print("\"");
                                writer.print(stringValue);
                                writer.print("\"");
                            } else {
                                writer.print(stringValue);
                            }
                        }
                    }
                    writer.println();
                }
            }

            System.out.println("Write table to " + tempFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
