package org.activityinfo.io.csv;

import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.model.query.ColumnSet;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CsvTableWriter implements AutoCloseable {

    public static final String CSV_MIME_TYPE = "text/csv";

    private final CsvWriter writer;

    public CsvTableWriter() throws IOException {
        this.writer = new CsvWriter();
    }

    public CsvTableWriter(Writer writer) throws IOException {
        this.writer = new CsvWriter(writer);
    }

    public void writeTable(EffectiveTableModel effectiveTableModel, ColumnSet columnSet) throws IOException {
        CsvColumnFactory columnFactory = new CsvColumnFactory(columnSet);
        List<CsvColumn> csvColumns = new ArrayList<>();
        for (EffectiveTableColumn effectiveTableColumn : effectiveTableModel.getColumns()) {
            csvColumns.addAll(effectiveTableColumn.accept(columnFactory));
        }
        writeHeaders(csvColumns);
        writeData(columnSet.getNumRows(), csvColumns);
    }

    private void writeHeaders(List<CsvColumn> csvColumns) throws IOException {
        writer.writeLine(csvColumns.stream().map(CsvColumn::getHeading).collect(Collectors.toList()));
    }

    private void writeData(int numRows, List<CsvColumn> csvColumns) throws IOException {
        for (int i=0; i<numRows; i++) {
            writeDataLine(i, csvColumns);
        }
    }

    private void writeDataLine(final int i, List<CsvColumn> csvColumns) throws IOException {
        writer.writeLine(csvColumns.stream().map(c -> {
            if (c.isMissing(i)) {
                return null;
            }
            return c.getValue(i);
        }).collect(Collectors.toList()));
    }

    public CsvWriter getWriter() {
        return writer;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
