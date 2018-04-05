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
