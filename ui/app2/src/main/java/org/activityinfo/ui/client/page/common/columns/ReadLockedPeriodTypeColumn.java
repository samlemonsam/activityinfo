package org.activityinfo.ui.client.page.common.columns;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.IsActivityDTO;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

/*
 * A column displaying an icon of the parent type of a LockedPeriod;
 * this can be a database, activity or a project
 */
public class ReadLockedPeriodTypeColumn extends ColumnConfig {

    public ReadLockedPeriodTypeColumn() {
        super();

        GridCellRenderer<LockedPeriodDTO> iconRenderer = new GridCellRenderer<LockedPeriodDTO>() {
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

                return SafeHtmlUtils.EMPTY_SAFE_HTML;
            }
        };
        setHeaderText(I18N.CONSTANTS.type());
        setToolTip(I18N.CONSTANTS.type());
        setWidth(48);
        setRowHeader(true);
        setRenderer(iconRenderer);
    }
}
