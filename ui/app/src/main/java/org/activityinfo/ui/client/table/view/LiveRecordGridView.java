package org.activityinfo.ui.client.table.view;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.grid.LiveGridView;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class LiveRecordGridView extends LiveGridView<Integer> {

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
            item.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    doSort(colIndex, SortDir.ASC);
                }
            });
            menu.add(item);

            item = new MenuItem();
            item.setText(DefaultMessages.getMessages().gridView_sortDescText());
            item.setIcon(header.getAppearance().sortDescendingIcon());
            item.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    doSort(colIndex, SortDir.DESC);
                }
            });
            menu.add(item);
        }

        MenuItem columns = new MenuItem();
        columns.setText(DefaultMessages.getMessages().gridView_columnsText());
        columns.setIcon(header.getAppearance().columnsIcon());
        columns.setData("gxt-columns", "true");

        menu.add(columns);
        return menu;
    }


}
