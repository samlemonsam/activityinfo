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

import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.menu.Menu;

public class DateRangeMenu extends Menu {

    private DatePicker date1;
    private DatePicker date2;

    public DateRangeMenu() {
        date1 = new DatePicker();
        date2 = new DatePicker();
        add(date1);
        add(date2);
        addStyleName("x-date-menu");
        setAutoHeight(true);
        plain = true;
        showSeparator = false;
        setEnableScrolling(false);
    }
}