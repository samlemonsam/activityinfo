package org.activityinfo.ui.client.table.view;

import com.google.gwt.dom.client.Node;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnHeader;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

public class CustomColumnHeader<M> extends ColumnHeader<M> {

    public CustomColumnHeader(Grid<M> container, ColumnModel<M> cm) {
        super(container, cm);
    }

    public CustomColumnHeader(Grid<M> container, ColumnModel<M> cm, ColumnHeaderAppearance appearance) {
        super(container, cm, appearance);
    }

    @Override
    protected Head createNewHead(ColumnConfig config) {
        return new Header(config);
    }

    public class Header extends Head {

        public Header(ColumnConfig column) {
            super(column);

            // Reorder the header text to appear *right* of the sort icon. Otherwise, the sort icon is lost
            // when a long header text string overflows, leaving the user unaware of sort status
            Node headerText = getElement().getChild(1);
            getElement().removeChild(headerText);
            getElement().appendChild(headerText);
        }

    }
}
