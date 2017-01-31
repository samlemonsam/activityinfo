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

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import java.util.Date;

public class LocalDateColumn extends ReadTextColumn {

    protected static final DateTimeFormat FORMAT = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_LONG);

    public LocalDateColumn(String property, String header, int width) {
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

                Object value = model.get(property);
                if (value == null) {
                    return SafeHtmlUtils.EMPTY_SAFE_HTML;
                }
                Date date;
                if (value instanceof Date) {
                    date = (Date) value;
                } else if (value instanceof LocalDate) {
                    date = ((LocalDate) value).atMidnightInMyTimezone();
                } else {
                    throw new RuntimeException("Don't know how to handle date as class " + value.getClass().getName());
                }
                return SafeHtmlUtils.fromTrustedString(FORMAT.format(date));
            }

        });
    }
}