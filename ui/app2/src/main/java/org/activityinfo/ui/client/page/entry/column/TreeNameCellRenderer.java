package org.activityinfo.ui.client.page.entry.column;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;
import org.activityinfo.legacy.shared.model.SiteDTO;

class TreeNameCellRenderer extends TreeGridCellRenderer<ModelData> {

    @Override
    public SafeHtml render(ModelData model,
                           String property,
                           ColumnData config,
                           int rowIndex,
                           int colIndex,
                           ListStore<ModelData> store,
                           Grid<ModelData> grid) {

        return super.render(model, propertyName(model), config, rowIndex, colIndex, store, grid);
    }

    private String propertyName(ModelData model) {
        if (model instanceof SiteDTO) {
            return SiteDTO.LOCATION_NAME_PROPERTY;
        } else {
            return "name";
        }
    }

}
