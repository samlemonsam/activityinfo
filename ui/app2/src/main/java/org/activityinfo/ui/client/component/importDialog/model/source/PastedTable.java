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
package org.activityinfo.ui.client.component.importDialog.model.source;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * An import source pasted in to a text field by the user.
 */
public class PastedTable implements SourceTable {

    private static final Logger LOGGER = Logger.getLogger(PastedTable.class.getName());

    public static final int HEADER_ROW_COUNT = 1;
    public static final int DEFAULT_ROW_PARSER_COUNT = 10;

    private final RowParser rowParser;

    private List<SourceColumn> columns;
    private final List<PastedRow> rows = Lists.newArrayList();
    private final DelimiterGuesser delimiterGuesser;

    public PastedTable(String text) {
        delimiterGuesser = new DelimiterGuesser(text);
        final char delimiter = delimiterGuesser.guess();
        this.rowParser = new RowParser(text, delimiter);
    }

    public int getFirstInvalidRow() {
        return delimiterGuesser.getFirstNotMatchedRow();
    }

    @Override
    public List<SourceColumn> getColumns() {
        ensureColumnsParsed();
        return columns;
    }

    private void ensureColumnsParsed() {
        if (columns == null) {
            // ensure header row is parsed
            if (rows.isEmpty()) {
                parseNextRows(HEADER_ROW_COUNT);
            }
            if (!rows.isEmpty()) {
                columns = new ColumnParser(rows.get(0)).parseColumns();
                rows.remove(0); // remove header row
            } else {
                columns = Lists.newArrayList();
            }
        }
    }

    private void ensureRowsParsed() {
        if (rowParser.eof() || rows.size() > DEFAULT_ROW_PARSER_COUNT) {
            ensureColumnsParsed();
            return;
        }

        parseNextRows(DEFAULT_ROW_PARSER_COUNT);
        ensureColumnsParsed();
    }

    /**
     * Parses all rows if not parsed yet. Otherwise if parsed do nothing.
     *
     * @return returns newly parsed rows (if nothing was parsed before returns all rows)
     */
    @Override
    public List<PastedRow> parseAllRows() {
        return parseNextRows(Integer.MAX_VALUE);
    }

    @Override
    public boolean parsedAllRows() {
        return rowParser.eof();
    }

    @Override
    public List<PastedRow> parseNextRows(int numberOfRowsToParse) {
        long startTime = new Date().getTime();
        List<PastedRow> parsedRows = rowParser.parseRows(numberOfRowsToParse);
        rows.addAll(parsedRows);
        LOGGER.fine("Parsed " + parsedRows.size() + " row(s), takes: " + (new Date().getTime() - startTime));
        return parsedRows;
    }

    @Override
    public List<? extends SourceRow> getRows() {
        ensureRowsParsed();
        return rows;
    }

    public String get(int row, int column) {
        ensureRowsParsed();
        int rowSize = rows.size();
        if (row > rowSize && rowParser.hasNextRow()) {
            parseNextRows(row - rowSize + 1);
        }
        return rows.get(row).getColumnValue(column);
    }

    @Override
    public String getColumnHeader(Integer columnIndex) {
        ensureColumnsParsed();
        return columns.get(columnIndex).getHeader();
    }
}
