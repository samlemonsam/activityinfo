package org.activityinfo.ui.client.component.table;
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

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.AbstractHeaderOrFooterBuilder;
import org.activityinfo.ui.client.component.table.action.ButtonActionCell;
import org.activityinfo.ui.client.component.table.action.TableHeaderAction;
import org.activityinfo.ui.client.widget.CellTableAffixer;

/**
 * @author yuriyz on 4/2/14.
 */
public class InstanceTableHeaderBuilder extends AbstractHeaderOrFooterBuilder<RowView> {

    public static final int ACTION_ROW_INDEX = 0;
    public static final int COLUMN_ROW_INDEX = 1;

    private final InstanceTable table;

    /**
     * Create a new InstanceTableHeaderBuilder for the header section.
     *
     * @param table the table being built
     */
    public InstanceTableHeaderBuilder(InstanceTable table) {
        super(table.getTable(), false);
        this.table = table;
    }

    @Override
    protected boolean buildHeaderOrFooterImpl() {
        int columnCount = getTable().getColumnCount();
        if (columnCount == 0) {
            // Nothing to render;
            return false;
        }

        buildActionRow(ACTION_ROW_INDEX, columnCount);
        buildColumnRow(COLUMN_ROW_INDEX, columnCount);
        return true;
    }

    private void buildColumnRow(int row, int columnCount) {

        final TableRowBuilder tr = startRow();
        setTrWidth(tr, row);

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            final FieldColumn column = (FieldColumn) getTable().getColumn(columnIndex);

            TableCellBuilder th = tr.startTH().className(InstanceTableStyle.INSTANCE.headerHover());
            setTdWidth(th, row, columnIndex);
            enableColumnHandlers(th, column);

            Cell.Context context = new Cell.Context(row, columnIndex, null);
            renderHeader(th, context, getTable().getHeader(columnIndex));

            th.endTH();
        }
        tr.endTR();
    }

    private void buildActionRow(int row, int columnCount) {
        AbstractCellTable.Style style = getTable().getResources().style();

        TableRowBuilder tr = startRow();
        setTrWidth(tr, row);

        TableCellBuilder th = tr.startTH().colSpan(columnCount).className(style.header());
        setTdWidth(th, row, 0);

        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendEscaped(table.getRootFormClass().getLabel());
        sb.appendHtmlConstant("&nbsp;");
        for (TableHeaderAction buttonAction : table.getHeaderActions()) {
            final ButtonActionCell cell = new ButtonActionCell(buttonAction);
            cell.render(new Cell.Context(row, 0, table), "", sb);
            sb.appendHtmlConstant("&nbsp;");
        }
        th.html(sb.toSafeHtml());

        th.endTH();
        tr.endTR();
    }

    private void setTdWidth(TableCellBuilder th, int row, int columnIndex) {
        final CellTableAffixer affixer = table.getTable().getAffixer();
        if (affixer != null) { // affixer can be null if table is not attached yet
            affixer.getWidthApplier().setTdWidth(th, row, columnIndex);
        }
    }

    private void setTrWidth(TableRowBuilder tr, int row) {
        final CellTableAffixer affixer = table.getTable().getAffixer();
        if (affixer != null) { // affixer can be null if table is not attached yet
            affixer.getWidthApplier().setTrWidth(tr, row);
        }
    }
}

