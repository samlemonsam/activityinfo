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
package org.activityinfo.ui.client.dispatch.type;

import com.google.gwt.i18n.client.NumberFormat;
import org.activityinfo.legacy.shared.type.IndicatorValueFormatter;

public class IndicatorNumberFormat implements IndicatorValueFormatter {

    public static final NumberFormat INSTANCE = NumberFormat.getFormat("#,##0.####");

    @Override
    public String format(Double value) {
        if (value == null) {
            return "";
        }
        return INSTANCE.format(value);
    }

}
