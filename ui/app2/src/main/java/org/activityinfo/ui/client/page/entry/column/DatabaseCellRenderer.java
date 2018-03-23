package org.activityinfo.ui.client.page.entry.column;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;

class DatabaseCellRenderer implements GridCellRenderer<SiteDTO> {

    private final SchemaDTO schema;

    public DatabaseCellRenderer(SchemaDTO schema) {
        this.schema = schema;
    }

    @Override
    public SafeHtml render(SiteDTO model,
                           String property,
                           ColumnData config,
                           int rowIndex,
                           int colIndex,
                           ListStore<SiteDTO> store,
                           Grid<SiteDTO> grid) {

        ActivityDTO activity = schema.getActivityById(model.getActivityId());
        return activity == null ?
                SafeHtmlUtils.EMPTY_SAFE_HTML :
                SafeHtmlUtils.fromString(activity.getDatabaseName());
    }
}
