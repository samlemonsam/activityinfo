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
package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.base.Strings;
import org.activityinfo.i18n.shared.I18N;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders a tree-based {@link PivotTable} into a two-dimensional grid.
 */
public class PivotTableRenderer {

    public static final String LINE_ENDING = "\r\n";

    private static class Coord {

        public Coord(int row, int column) {
            this.row = row;
            this.column = column;
        }

        int row;
        int column;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Coord coord = (Coord) o;

            if (row != coord.row) return false;
            return column == coord.column;

        }

        @Override
        public int hashCode() {
            int result = row;
            result = 31 * result + column;
            return result;
        }
    }

    private final Map<Coord, String> cells = new HashMap<>();
    private final List<PivotTable.Node> leafColumns;
    private int numRows;
    private int numCols;


    private PivotTableRenderer(PivotTable table) {
        this.leafColumns = table.getRootColumn().getLeaves();
        int numHeaderCols = table.getRowDimensions().size() * 2;
        int numHeaderRows = table.getColumnDimensions().size() * 2;

        addColumnHeaders(table.getRootColumn(), 0, numHeaderCols);
        addRowHeaders(table.getRootRow(), numHeaderRows, 0);
        addValues(table.getRootRow(), numHeaderRows, 0);
    }



    private int addColumnHeaders(PivotTable.Node parent, int row, int col) {
        if(!parent.isLeaf()) {
            addCell(row, col, parent.getDimension().getLabel());
            for (PivotTable.Node child : parent.getChildren()) {
                addCell(row+1, col, child.getCategoryLabel());
                if(child.isLeaf()) {
                    col++;
                } else {
                    col = addColumnHeaders(child, row+2, col);
                }
            }
        }
        return col;
    }

    public static String renderPlainText(PivotTable table) {
        return new PivotTableRenderer(table).renderPlainText();
    }

    public static String renderDelimited(PivotTable table, String delimiter) {
        return new PivotTableRenderer(table).renderDelimited(delimiter);
    }



    private int addRowHeaders(PivotTable.Node parent, int row, int col) {
        if(parent.getDimension() != null) {
            addCell(row, col, parent.getDimension().getLabel());
        }
        for (PivotTable.Node child : parent.getChildren()) {
            addCell(row, col+1, child.getCategoryLabel());
            if(child.isLeaf()) {
                row++;
            } else {
                row = addRowHeaders(child, row, col + 2);
            }
        }
        return row;
    }


    private int addValues(PivotTable.Node parentRow, int row, int col) {
        if(parentRow.isLeaf()) {
            for (Map.Entry<PivotTable.Node, Point> entry : parentRow.getPoints().entrySet()) {
                int leafIndex = leafColumns.indexOf(entry.getKey());

                addCell(row, col + leafIndex, entry.getValue().getFormattedValue());
            }
            row++;
        } else {
            for (PivotTable.Node child : parentRow.getChildren()) {
                row = addValues(child, row, col+2);
            }
        }
        return row;
    }

    private void addCell(int row, int col, String label) {
        cells.put(new Coord(row, col), label);
        if(row+1 > numRows) {
            numRows = row+1;
        }
        if(col+1 > numCols) {
            numCols = col+1;
        }
    }

    public String renderDelimited(String delimiter) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if(j > 0) {
                    s.append(delimiter);
                }
                String value = cells.get(new Coord(i,j));
                if(value != null) {
                    s.append(value);
                }
            }
            s.append(LINE_ENDING);
        }

        return s.toString();
    }

    public String renderPlainText() {
        int columnWidths[] = new int[numCols];
        for (Map.Entry<Coord, String> entry : cells.entrySet()) {
            int col = entry.getKey().column;
            int width = entry.getValue().length();
            if(width > columnWidths[col]) {
                columnWidths[col] = width;
            }
        }

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                int width = columnWidths[j] + 3;
                String value = Strings.nullToEmpty(cells.get(new Coord(i,j)));
                s.append(Strings.padEnd(value, width, ' '));
            }
            s.append('\n');
        }

        return s.toString();
    }
}
