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
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;

import java.util.HashMap;
import java.util.Map;

class ActivityCellRenderer implements GridCellRenderer<SiteDTO> {

    private final Map<Integer, String> activityNameMap = new HashMap<>();

    public ActivityCellRenderer(UserDatabaseDTO database) {
        for (ActivityDTO activity : database.getActivities()) {
            activityNameMap.put(activity.getId(), activity.getName());
        }
    }

    public ActivityCellRenderer(SchemaDTO schema) {
        for (UserDatabaseDTO database : schema.getDatabases()) {
            for (ActivityDTO activity : database.getActivities()) {
                activityNameMap.put(activity.getId(), activity.getName());
            }
        }
    }

    @Override
    public SafeHtml render(SiteDTO model,
                           String property,
                           ColumnData config,
                           int rowIndex,
                           int colIndex,
                           ListStore<SiteDTO> store,
                           Grid<SiteDTO> grid) {

        String activity = activityNameMap.get(model.getActivityId());
        return activity == null ?
                SafeHtmlUtils.EMPTY_SAFE_HTML :
                SafeHtmlUtils.fromString(activity);
    }
}
