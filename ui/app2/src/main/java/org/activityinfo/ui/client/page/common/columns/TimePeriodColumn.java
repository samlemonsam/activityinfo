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
package org.activityinfo.ui.client.page.common.columns;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;
import org.activityinfo.i18n.shared.I18N;

import static com.google.gwt.safehtml.shared.SafeHtmlUtils.fromTrustedString;

public class TimePeriodColumn extends ReadTextColumn {

    public TimePeriodColumn(String property, String header, int width) {
        super(property, header, width);

        setId(property);
        setHeaderText(header);
        setToolTip(header);
        setWidth(width);
        setRowHeader(true);
        setRenderer(new GridCellRenderer<ModelData>() {

            @Override
            public SafeHtml render(ModelData model,
                                   String property,
                                   ColumnData config,
                                   int rowIndex,
                                   int colIndex,
                                   ListStore<ModelData> store,
                                   Grid<ModelData> grid) {

                LocalDate fromDate = model.get("fromDate");
                LocalDate toDate = model.get("toDate");

                return fromTrustedString(
                        I18N.MESSAGES.dateRange(fromDate.atMidnightInMyTimezone(), toDate.atMidnightInMyTimezone()));
            }
        });
    }
}