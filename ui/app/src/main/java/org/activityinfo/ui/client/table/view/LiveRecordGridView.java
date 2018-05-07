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
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.grid.LiveGridView;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class LiveRecordGridView extends LiveGridView<Integer> {

    /**
     * Creates a context menu for the given column, including sort menu items and column visibility sub-menu.
     *
     * @param colIndex the column index
     * @return the context menu for the given column
     */
    @Override
    protected Menu createContextMenu(final int colIndex) {
        final Menu menu = new Menu();

        if (cm.isSortable(colIndex)) {
            MenuItem item = new MenuItem();
            item.setText(DefaultMessages.getMessages().gridView_sortAscText());
            item.setIcon(header.getAppearance().sortAscendingIcon());
            item.addSelectionHandler(select -> sort(colIndex, SortDir.ASC));
            menu.add(item);

            item = new MenuItem();
            item.setText(DefaultMessages.getMessages().gridView_sortDescText());
            item.setIcon(header.getAppearance().sortDescendingIcon());
            item.addSelectionHandler(select -> sort(colIndex, SortDir.DESC));
            menu.add(item);
        }

        return menu;
    }

    private void sort(int colIndex, SortDir dir) {
        ValueProvider vp = cm.getColumn(colIndex).getValueProvider();
        Store.StoreSortInfo sortInfo = new Store.StoreSortInfo(vp, dir);
        grid.getStore().clearSortInfo();
        grid.getStore().addSortInfo(sortInfo);
    }

    @Override
    protected void templateOnColumnWidthUpdated(int col, int w, int tw) {
        fireEvent(new ColumnResizeEvent(col, w));
    }

    public HandlerRegistration addColumnResizeHandler(ColumnResizeHandler handler) {
        return addHandler(ColumnResizeEvent.getType(), handler);
    }
}
