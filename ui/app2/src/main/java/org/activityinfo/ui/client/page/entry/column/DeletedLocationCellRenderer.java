package org.activityinfo.ui.client.page.entry.column;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;

class DeletedLocationCellRenderer extends TreeGridCellRenderer<ModelData> {

    private static final String WARNING_STYLE = "color:tomato; font-size:20px; font-weight:bold";

    @Override
    public SafeHtml render(ModelData model,
                           String property,
                           ColumnData config,
                           int rowIndex,
                           int colIndex,
                           ListStore<ModelData> store,
                           Grid<ModelData> grid) {
        if (model instanceof SiteDTO) {
            String workflowStatus = ((SiteDTO) model).getLocation().getWorkflowStatusId();
            if (!Strings.isNullOrEmpty(workflowStatus) && workflowStatus.equals(LocationDTO.REJECTED)) {
                return SafeHtmlUtils.fromSafeConstant("<div style='" + WARNING_STYLE + "' title='" + I18N.CONSTANTS.deletedLocation() + "'>&nbsp;!&nbsp;</div>");
            }
        }
        return SafeHtmlUtils.fromSafeConstant("&nbsp;");
    }
}
