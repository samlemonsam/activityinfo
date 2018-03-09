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
package org.activityinfo.ui.client.page.entry;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.google.common.base.Predicate;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.IndicatorRowDTO;
import org.activityinfo.model.type.time.Month;
import org.activityinfo.ui.client.dispatch.type.IndicatorNumberFormat;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Grid for use in the MonthlyTab
 */
class MonthlyGrid extends EditorGrid<IndicatorRowDTO> {

    private static final int MONTHS_TO_SHOW = 7;

    private static final int ROW_HEADER_WIDTH = 150;
    private static final int MONTH_COLUMN_WIDTH = 75;
    private boolean readOnly = true;
    private Predicate<Month> locked;
    private final DateTimeFormat monthFormat = DateTimeFormat.getFormat("MMM yy");

    public MonthlyGrid(ListStore<IndicatorRowDTO> store) {
        super(store, createColumnModel());

        setAutoExpandColumn("indicatorName");
        setLoadMask(true);
        setStripeRows(true);
        setColumnLines(true);
        addListener(Events.BeforeEdit, new Listener<GridEvent>() {
            @Override
            public void handleEvent(GridEvent event) {
                if (readOnly) {
                    event.setCancelled(true);
                }
                String property = getColumnModel().getColumn(event.getColIndex()).getDataIndex();
                Month month = IndicatorRowDTO.monthForProperty(property);
                if (locked.apply(month)) {
                    event.setCancelled(true);
                }
            }
        });
    }

    /**
     * Updates the month headers based on the given start month
     */
    public void updateMonthColumns(Month startMonth) {

        Month month = startMonth;
        for (int i = 0; i != MONTHS_TO_SHOW; ++i) {
            getColumnModel().setColumnHeader(i + 1, formatHeader(month));
            getColumnModel().setDataIndex(i + 1, IndicatorRowDTO.propertyName(month));
            month = month.next();
        }
    }

    private SafeHtml formatHeader(Month month) {
        DateWrapper date = new DateWrapper(month.getYear(), month.getMonth() - 1, 1);
        String header = monthFormat.format(date.asDate());
        if (locked.apply(month)) {
            header = IconImageBundle.ICONS.lockedPeriodSmall().getHTML() + " " + header;
        }
        return SafeHtmlUtils.fromTrustedString(header);
    }

    private static ColumnModel createColumnModel() {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        ColumnConfig indicator = new ColumnConfig("indicatorName", I18N.CONSTANTS.indicators(), ROW_HEADER_WIDTH);
        indicator.setSortable(false);
        indicator.setMenuDisabled(true);
        columns.add(indicator);

        for (int i = 0; i != MONTHS_TO_SHOW; ++i) {
            NumberField indicatorField = new NumberField();
            indicatorField.getPropertyEditor().setFormat(IndicatorNumberFormat.INSTANCE);

            ColumnConfig valueColumn = new ColumnConfig("month" + i, "", MONTH_COLUMN_WIDTH);
            valueColumn.setNumberFormat(IndicatorNumberFormat.INSTANCE);
            valueColumn.setEditor(new CellEditor(indicatorField));
            valueColumn.setSortable(false);
            valueColumn.setMenuDisabled(true);
            columns.add(valueColumn);
        }

        return new ColumnModel(columns);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setLockedPredicate(Predicate<Month> locked) {
        this.locked = locked;
    }
}
