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
package org.activityinfo.ui.client.table.view;

import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.grid.ColumnHeader;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.LiveGridView;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.query.SortModel;

import java.util.Collections;
import java.util.List;

public class LiveRecordGridView extends LiveGridView<Integer> {

    private List<SortModel> currentSorting = Collections.emptyList();

    /**
     * Creates a context menu for the given column, including sort menu items and column visibility sub-menu.
     *
     * @param colIndex the column index
     * @return the context menu for the given column
     */
    protected Menu createContextMenu(final int colIndex) {
        final Menu menu = new Menu();

        if (cm.isSortable(colIndex)) {
            MenuItem item = new MenuItem();
            item.setText(DefaultMessages.getMessages().gridView_sortAscText());
            item.setIcon(header.getAppearance().sortAscendingIcon());
            item.addSelectionHandler(event -> doSort(colIndex, SortDir.ASC));
            menu.add(item);

            item = new MenuItem();
            item.setText(DefaultMessages.getMessages().gridView_sortDescText());
            item.setIcon(header.getAppearance().sortDescendingIcon());
            item.addSelectionHandler(event -> doSort(colIndex, SortDir.DESC));
            menu.add(item);

            item = new MenuItem();
            item.setText(I18N.CONSTANTS.clearSort());
            item.addSelectionHandler(event -> fireEvent(new SortChangeEvent()));
            menu.add(item);
        }

        return menu;
    }

    @Override
    protected void templateOnColumnWidthUpdated(int col, int w, int tw) {
        fireEvent(new ColumnResizeEvent(col, w));
    }

    public HandlerRegistration addColumnResizeHandler(ColumnResizeHandler handler) {
        return addHandler(ColumnResizeEvent.getType(), handler);
    }

    public HandlerRegistration addSortChangeHandler(SortChangeHandler handler) {
        return addHandler(SortChangeEvent.getType(), handler);
    }

    @Override
    protected void doSort(int colIndex, SortDir sortDir) {
        String field = grid.getColumnModel().getValueProvider(colIndex).getPath();
        org.activityinfo.model.query.SortDir dir;

        // sortDir is null if the column header is clicked to
        // toggle the sort direction

        if(sortDir == null) {
            if(isFieldSortedAscending(field)) {
                dir = org.activityinfo.model.query.SortDir.DESC;
            } else {
                dir = org.activityinfo.model.query.SortDir.ASC;
            }
        } else if(sortDir == SortDir.ASC) {
            dir = org.activityinfo.model.query.SortDir.ASC;
        } else {
            dir = org.activityinfo.model.query.SortDir.DESC;
        }

        fireEvent(new SortChangeEvent(field, dir));
    }

    private boolean isFieldSortedAscending(String field) {
        if(!currentSorting.isEmpty()) {
            SortModel sortModel = currentSorting.get(0);
            if(sortModel.getField().equals(field) && sortModel.getDir() == org.activityinfo.model.query.SortDir.ASC) {
                return true;
            }
        }
        return false;
    }

    public void maybeUpdateSortingView(List<SortModel> sorting) {

        if(!currentSorting.equals(sorting)) {

            ColumnModel<Integer> columnModel = grid.getColumnModel();
            ColumnHeader<Integer> tableHeading = grid.getView().getHeader();

            if (sorting.isEmpty()) {
                tableHeading.updateSortIcon(-1, SortDir.ASC);
            } else {
                SortModel sortModel = sorting.get(0);
                for (int i = 0; i < columnModel.getColumnCount(); i++) {
                    String path = columnModel.getValueProvider(i).getPath();
                    if (path.equals(sortModel.getField())) {
                        tableHeading.updateSortIcon(i,
                                sortModel.getDir() == org.activityinfo.model.query.SortDir.ASC ?
                                        SortDir.ASC : SortDir.DESC);
                        break;
                    }
                }
            }
            currentSorting = sorting;
        }
    }

    @Override
    protected void initHeader() {
        if (header == null) {
            header = new CustomColumnHeader<Integer>(grid, cm);
        }
        header.setMenuFactory(new ColumnHeader.HeaderContextMenuFactory() {
            @Override
            public Menu getMenuForColumn(int columnIndex) {
                return createContextMenu(columnIndex);
            }
        });
        header.setSplitterWidth(splitterWidth);
    }
}
