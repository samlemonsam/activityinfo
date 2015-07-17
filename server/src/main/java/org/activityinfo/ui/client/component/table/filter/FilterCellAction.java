package org.activityinfo.ui.client.component.table.filter;
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

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.user.client.ui.PopupPanel;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.client.widget.CellTable;

/**
 * @author yuriyz on 4/3/14.
 */
public class FilterCellAction implements ActionCell.Delegate {

    private static final int MINIMUM_POPUP_WIDTH_PX = 250;

    private final InstanceTable table;
    private final FieldColumn column;

    public FilterCellAction(InstanceTable table, FieldColumn column) {
        this.table = table;
        this.column = column;
    }

    @Override
    public void execute(Object object) {
        final CellTable<Projection> grid = table.getTable();
        final TableSectionElement tableHeadElement = grid.getTableHeadElement();
        final FilterPanel filterPanel = new FilterPanel(table, column);
        filterPanel.show(new PopupPanel.PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                // get second row (first row is header action row)
                final TableRowElement row = tableHeadElement.getRows().getItem(1);
                final int columnIndex = grid.getColumnIndex(column);
                final TableCellElement cellElement = row.getCells().getItem(columnIndex);
                final int absoluteTop = cellElement.getAbsoluteTop();
                final int absoluteLeft = cellElement.getAbsoluteLeft();
                final int height = cellElement.getOffsetHeight();
                int width = cellElement.getOffsetWidth();
                if (width < MINIMUM_POPUP_WIDTH_PX) {
                    width = MINIMUM_POPUP_WIDTH_PX;
                }

//                filterPanel.getPopup().setAutoHideEnabled(false);
                filterPanel.getPopup().setWidth(width + "px");
                filterPanel.getPopup().setPopupPosition(absoluteLeft,
                        absoluteTop + height);
            }
        });
    }

    public InstanceTable getTable() {
        return table;
    }

    public FieldColumn getColumn() {
        return column;
    }
}
