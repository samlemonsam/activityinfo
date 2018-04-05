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
package org.activityinfo.ui.client.page.config;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

class LockTypeIconCellRenderer implements GridCellRenderer<LockedPeriodDTO> {
    @Override
    public SafeHtml render(LockedPeriodDTO model,
                           String property,
                           ColumnData config,
                           int rowIndex,
                           int colIndex,
                           ListStore<LockedPeriodDTO> store,
                           Grid<LockedPeriodDTO> grid) {

        if (model.getParent() instanceof IsActivityDTO) {
            return IconImageBundle.ICONS.form().getSafeHtml();
        }

        if (model.getParent() instanceof UserDatabaseDTO) {
            return IconImageBundle.ICONS.database().getSafeHtml();
        }

        if (model.getParent() instanceof ProjectDTO) {
            return IconImageBundle.ICONS.project().getSafeHtml();
        }

        if (model.getParent() instanceof FolderDTO) {
            return IconImageBundle.ICONS.folder().getSafeHtml();
        }

        return SafeHtmlUtils.EMPTY_SAFE_HTML;
    }
}
