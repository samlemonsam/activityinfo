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
package org.activityinfo.ui.client.component.importDialog.mapping;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.AbstractHeaderOrFooterBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;

/**
 * Builds a two-row header, with the first row showing the original column names, and the
 * second show the field to which it is mapped
 */
class GridHeaderBuilder extends AbstractHeaderOrFooterBuilder<SourceRow> {

    /**
     * Create a new DefaultHeaderBuilder for the header of footer section.
     *
     * @param table    the table being built
     */
    public GridHeaderBuilder(AbstractCellTable<SourceRow> table) {
        super(table, /* isFooter = */ false);
    }

    @Override
    protected boolean buildHeaderOrFooterImpl() {

        // we may not have a source yet...
        if(getTable().getColumnCount() == 0) {
            return false;
        }

        renderHeaderRow(ColumnMappingGrid.SOURCE_COLUMN_HEADER_ROW, ColumnMappingStyles.INSTANCE.sourceColumnHeader());
        renderHeaderRow(ColumnMappingGrid.MAPPING_HEADER_ROW, ColumnMappingStyles.INSTANCE.mappingHeader());

        return true;
    }

    private void renderHeaderRow(int headerRowIndex, String className) {
        TableRowBuilder tr = startRow();

        int curColumn;
        int columnCount = getTable().getColumnCount();
        for (curColumn = 0; curColumn < columnCount; curColumn++) {
            Header<?> header = getHeader(curColumn);
            Column<SourceRow, ?> column = getTable().getColumn(curColumn);

            // Render the header.
            TableCellBuilder th = tr.startTH().className(className);
            enableColumnHandlers(th, column);

            // Build the header.
            Cell.Context context = new Cell.Context(headerRowIndex, curColumn, null);
            renderHeader(th, context, header);

            th.endTH();
        }
        tr.end();
    }
}
