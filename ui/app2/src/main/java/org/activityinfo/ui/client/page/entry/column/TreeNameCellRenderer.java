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
