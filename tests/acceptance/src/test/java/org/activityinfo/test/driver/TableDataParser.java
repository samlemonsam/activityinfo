package org.activityinfo.test.driver;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cucumber.api.DataTable;
import gherkin.formatter.model.DataTableRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 07/14/2015.
 */
public class TableDataParser {


    public static DataTable exportedDataTableFromCsvFile(File file) throws IOException {

        List<List<String>> rows = new ArrayList<>();
        CSVParser parser = null;

        try (Reader reader = new FileReader(file)) {
            parser = CSVFormat.EXCEL.parse(reader);
            for (CSVRecord record : parser.getRecords()) {
                List<String> row = Lists.newArrayList();
                for (int i = 0; i < record.size(); i++) {
                    row.add(record.get(i));
                }
                rows.add(row);
            }
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
        return DataTable.create(rows);
    }

    public static DataTable exportedDataTable(File file) throws IOException, InvalidFormatException {

        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();

        List<List<String>> rows = new ArrayList<>();

        // First row contains a title
        int numRowsToSkip = 1;

        // Find the number of columns
        int numColumns = 0;
        for (int rowIndex = numRowsToSkip; rowIndex <= sheet.getLastRowNum(); ++rowIndex) {
            numColumns = Math.max(numColumns, sheet.getRow(rowIndex).getLastCellNum());
        }

        // Create the table
        for (int rowIndex = numRowsToSkip; rowIndex <= sheet.getLastRowNum(); ++rowIndex) {
            List<String> row = new ArrayList<>();
            Row excelRow = sheet.getRow(rowIndex);

            for (int colIndex = 0; colIndex < numColumns; ++colIndex) {
                row.add(formatter.formatCellValue(excelRow.getCell(colIndex)));
            }
            rows.add(row);
        }

        if (rows.isEmpty()) {
            throw new AssertionError("Export contained no data");
        }

        return DataTable.create(rows);
    }

    public static String getFirstColumnValue(DataTable table, String columnName) {
        List<String> values = getColumnValues(table, columnName);
        return values.size() > 2 ? values.get(2) : null; // first row is header, second row is type
    }

    public static List<String> getColumnValues(DataTable table, String columnName) {
        List<String> headerRow = table.getGherkinRows().get(0).getCells();

        int columnIndex = -1;
        for (int i = 0; i < headerRow.size(); i++) {
            columnIndex = i;
            if (headerRow.get(i).equalsIgnoreCase(columnName)) {
                break;
            }
        }

        List<String> columnCells = Lists.newArrayList();
        if (columnIndex != -1) {
            for (DataTableRow row : table.getGherkinRows()) {
                columnCells.add(row.getCells().get(columnIndex));
            }
        }
        return columnCells;
    }

    public static Map<String, String> asMap(DataTable table) {
        Map<String, String> map = Maps.newHashMap();
        for (int i = 1; i < table.getGherkinRows().size(); i++) {
            List<String> cells = table.getGherkinRows().get(i).getCells();
            Preconditions.checkState(cells.size() == 2, "It's expected to have exactly 2 columns (key, value)");

            map.put(cells.get(0), cells.get(1));
        }
        return map;
    }
}

