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
package org.activityinfo.ui.client.component.report.editor.map.layerOptions;

import com.google.gwt.user.client.Event;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.ui.client.component.filter.FilterResources;
import org.activityinfo.ui.client.component.filter.FilterWidget;
import org.activityinfo.ui.client.component.filter.SelectionCallback;

public class DateFilterWidget extends FilterWidget {

    private DateFilterMenu menu;

    public DateFilterWidget() {
        dimensionSpan.setInnerHTML(I18N.CONSTANTS.dates());
        stateSpan.setInnerText(I18N.CONSTANTS.allDates());
    }

    @Override
    public void updateView() {
        
        DateRange range = getValue().getEndDateRange();
        
        if (range.getMinDate() == null && range.getMaxDate() == null) {
            setState(I18N.CONSTANTS.allDates());
        } else if (range.getMinDate() == null) {
            setState(FilterResources.MESSAGES.beforeDate(range.getMaxDate()));
        } else if (range.getMaxDate() == null) {
            setState(FilterResources.MESSAGES.afterDate(range.getMinDate()));
        } else {
            setState(FilterResources.MESSAGES.betweenDates(range.getMinDate(), range.getMaxDate()));
        }
    }

    @Override
    public void choose(Event event) {
        if (menu == null) {
            menu = new DateFilterMenu();
        }
        menu.showAt(event.getClientX(), event.getClientY(), new SelectionCallback<DateRange>() {

            @Override
            public void onSelected(DateRange selection) {
                Filter filter = new Filter(getValue());
                filter.setEndDateRange(selection);

                setValue(filter);
            }
        });
    }

}
