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
