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

import com.google.gwt.i18n.client.DateTimeFormat;
import org.activityinfo.ui.client.component.importDialog.model.type.formatter.DateFormatter;
import org.activityinfo.ui.client.component.importDialog.model.type.formatter.DateFormatterFactory;

import java.util.Date;

/**
 * @author yuriyz on 3/10/14.
 */
public class JsDateFormatterFactory implements DateFormatterFactory {

    public static final DateTimeFormat GWT_FORMAT = DateTimeFormat.getFormat(FORMAT);

    @Override
    public DateFormatter create() {
        return new DateFormatter() {
            @Override
            public String format(Date value) {
                return GWT_FORMAT.format(value);
            }

            @Override
            public Date parse(String valueAsString) {
                return GWT_FORMAT.parse(valueAsString);
            }
        };
    }
}
