package org.activityinfo.analysis.pivot;

import com.google.common.collect.Maps;
import org.activityinfo.analysis.pivot.viewModel.*;
import org.activityinfo.model.analysis.pivot.Axis;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.stream.Collectors;

public class PivotTableWriter implements AutoCloseable {

    /**
     * Writes a byte order mark that should help spreadsheet software detect the UTF-8 character set.
     */
    public static final char BYTEORDER_MARK = '\ufeff';
    public static final String DELIMITER = ",";
    public static final String LINE_ENDING = "\r\n";

    private static final String VALUE_COL = "Value";

    private final Writer writer;

    Map<String,Integer> rowDims = Maps.newHashMap();
    Map<String,Integer> colDims = Maps.newHashMap();

    public PivotTableWriter(Writer writer) throws IOException {
        this.writer = writer;
        writerByteOrderMark();
    }

    public void write(AnalysisResult pivotTable) throws IOException {
        constructDimensionIndexMapping(pivotTable);
        writeRowDimensionHeaders();
        writeColDimensionHeaders();
        writePoints(pivotTable);
    }

    private void constructDimensionIndexMapping(AnalysisResult pivotTable) {
        rowDims = extractDimensionIndex(pivotTable, Axis.ROW);
        colDims = extractDimensionIndex(pivotTable, Axis.COLUMN);
    }

    private Map<String,Integer> extractDimensionIndex(AnalysisResult pivotTable, Axis axis) {
        return pivotTable.getDimensionSet().getList().stream()
                .filter(dimension -> axis.equals(dimension.getAxis()))
                .collect(Collectors.toMap(
                        dimension -> dimension.getLabel(),
                        dimension -> pivotTable.getDimensionSet().getIndex(dimension)));
    }

    private void writeRowDimensionHeaders() {
        rowDims.keySet().forEach(this::writeDelimited);
    }

    private void writeColDimensionHeaders() {
        colDims.keySet().forEach(this::writeDelimited);
        writeValueColumnHeader();
    }

    private void writeValueColumnHeader() {
        write(VALUE_COL);
        write(LINE_ENDING);
    }

    private void writePoints(AnalysisResult pivotTable) {
        pivotTable.getPoints().forEach(this::writePoint);
        write(LINE_ENDING);
    }

    private void writePoint(Point point) {
        writeDims(rowDims, point);
        writeDims(colDims, point);
        writeValue(point);
    }

    private void writeValue(Point point) {
        write(point.getFormattedValue());
        write(LINE_ENDING);
    }

    private void writeDims(Map<String, Integer> dims, Point point) {
        dims.values().stream()
                .map(point::getCategory)
                .forEach(this::writeDelimited);
    }

    private void writerByteOrderMark() throws IOException {
        writer.append(BYTEORDER_MARK);
    }

    private void writeDelimited(String data) {
        write(data);
        write(DELIMITER);
    }

    private void write(String data) {
        try {
            writer.append(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Writer getWriter() {
        return writer;
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }
}
