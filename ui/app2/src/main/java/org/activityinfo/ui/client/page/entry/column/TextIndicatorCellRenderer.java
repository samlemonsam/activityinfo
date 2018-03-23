package org.activityinfo.ui.client.page.entry.column;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

class TextIndicatorCellRenderer implements GridCellRenderer {
    @Override
    public SafeHtml render(ModelData model,
                           String property,
                           ColumnData config,
                           int rowIndex,
                           int colIndex,
                           ListStore listStore,
                           Grid grid) {

        Object value = model.get(property);
        if(value instanceof String) {
            return SafeHtmlUtils.fromString((String) value);
        } else {
            return SafeHtmlUtils.EMPTY_SAFE_HTML;
        }
    }
}
