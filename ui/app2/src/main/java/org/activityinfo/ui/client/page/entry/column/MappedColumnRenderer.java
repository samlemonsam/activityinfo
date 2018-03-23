package org.activityinfo.ui.client.page.entry.column;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.legacy.shared.model.SiteDTO;

class MappedColumnRenderer implements GridCellRenderer<ModelData> {
    @Override
    public SafeHtml render(ModelData model,
                           String property,
                           ColumnData config,
                           int rowIndex,
                           int colIndex,
                           ListStore listStore,
                           Grid grid) {
        if (model instanceof SiteDTO) {
            SiteDTO siteModel = (SiteDTO) model;
            if (siteModel.hasCoords()) {
                return SafeHtmlUtils.fromSafeConstant("<div class='mapped'>&nbsp;&nbsp;</div>");
            } else {
                return SafeHtmlUtils.fromSafeConstant("<div class='unmapped'>&nbsp;&nbsp;</div>");
            }
        }
        return SafeHtmlUtils.fromSafeConstant("&nbsp;");
    }
}
