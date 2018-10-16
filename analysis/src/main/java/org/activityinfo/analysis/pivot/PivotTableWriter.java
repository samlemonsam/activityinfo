package org.activityinfo.analysis.pivot;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.activityinfo.analysis.pivot.viewModel.*;
import org.activityinfo.model.analysis.pivot.Axis;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class PivotTableWriter implements AutoCloseable {

    /**
     * Writes a byte order mark that should help spreadsheet software detect the UTF-8 character set.
     */
    public static final char BYTEORDER_MARK = '\ufeff';

    public static final String DELIMITER = ",";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String LINE_ENDING = "\r\n";

    private static final String FOLDER_COL = "Folder";
    private static final String VALUE_COL = "Value";

    private final Writer writer;

    private List<Integer> rowDims = Lists.newArrayList();
    private List<Integer> colDims = Lists.newArrayList();
    private Integer formIdDimension;

    public PivotTableWriter(Writer writer) throws IOException {
        this.writer = writer;
        writerByteOrderMark();
    }

    public void write(AnalysisResult pivotTable, @Nullable Map<ResourceId,String> folderMapping) throws IOException {
        constructDimensionIndexMapping(pivotTable.getDimensionSet());
        writeRowDimensionHeaders(pivotTable.getDimensionSet(), folderMapping);
        writeColDimensionHeaders(pivotTable.getDimensionSet());
        writePoints(pivotTable, folderMapping);
    }

    private void constructDimensionIndexMapping(DimensionSet dimensionSet) {
        rowDims = extractDimensionIndex(dimensionSet, Axis.ROW);
        colDims = extractDimensionIndex(dimensionSet, Axis.COLUMN);
        formIdDimension = extractFormIdDimensionIndex(dimensionSet);
    }

    private Integer extractFormIdDimensionIndex(DimensionSet dimensionSet) {
        return dimensionSet.getList().stream()
                .filter(dimension -> dimension.getLabel().equals("FormId"))
                .map(dimensionSet::getIndex)
                .findFirst().orElse(null);
    }

    private List<Integer> extractDimensionIndex(DimensionSet dimensionSet, Axis axis) {
        return dimensionSet.getList().stream()
                .filter(dimension -> axis.equals(dimension.getAxis()))
                .map(dimensionSet::getIndex)
                .collect(Collectors.toList());
    }

    private void writeRowDimensionHeaders(DimensionSet dimensionSet, Map<ResourceId, String> folderMapping) {
        if (folderMapping != null) {
            writeFolderColumnHeader();
        }
        rowDims.forEach(rowDim -> writeDelimited(dimensionSet.getDimension(rowDim).getLabel()));
    }

    private void writeColDimensionHeaders(DimensionSet dimensionSet) {
        colDims.forEach(colDim -> writeDelimited(dimensionSet.getDimension(colDim).getLabel()));
        writeValueColumnHeader();
    }

    private void writeFolderColumnHeader() {
        write(FOLDER_COL, false);
        write(DELIMITER, true);
    }

    private void writeValueColumnHeader() {
        write(VALUE_COL, false);
        write(LINE_ENDING, true);
    }

    private void writePoints(AnalysisResult pivotTable, Map<ResourceId, String> folderMapping) {
        pivotTable.getPoints().forEach(point -> {
            if (folderMapping != null) {
                writeFolder(point, folderMapping);
            }
            writePoint(point);
        });
        write(LINE_ENDING, true);
    }

    private void writeFolder(Point point, Map<ResourceId, String> folderMapping) {
        ResourceId formIdPoint = ResourceId.valueOf(point.getCategory(formIdDimension));
        if (formIdPoint.getDomain() == CuidAdapter.MONTHLY_REPORT_FORM_CLASS) {
            int monthlyActivityId = CuidAdapter.getLegacyIdFromCuid(formIdPoint);
            formIdPoint = CuidAdapter.activityFormClass(monthlyActivityId);
        }
        writeDelimited(folderMapping.getOrDefault(formIdPoint, ""));
    }

    private void writePoint(Point point) {
        writeDims(rowDims, point);
        writeDims(colDims, point);
        writeValue(point);
    }

    private void writeValue(Point point) {
        write(point.getFormattedValue(), false);
        write(LINE_ENDING, true);
    }

    private void writeDims(List<Integer> dims, Point point) {
        dims.stream()
                .map(point::getCategory)
                .forEach(this::writeDelimited);
    }

    private void writerByteOrderMark() throws IOException {
        writer.append(BYTEORDER_MARK);
    }

    @VisibleForTesting
    protected void writeDelimited(String data) {
        write(data, false);
        write(DELIMITER, true);
    }


    private void write(String data, boolean specialChar) {
        try {
            if (specialChar) {
                writer.append(data);
            } else {
                writer.append(escapeIfNecessary(data));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String escapeIfNecessary(String data) {
        if (data.contains(DELIMITER) || data.contains(DOUBLE_QUOTE) || data.contains(LINE_ENDING)) {
            return enquote(data);
        } else {
            return data;
        }
    }

    private String enquote(String data) {
        return DOUBLE_QUOTE + data + DOUBLE_QUOTE;
    }

    public Writer getWriter() {
        return writer;
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }
}
