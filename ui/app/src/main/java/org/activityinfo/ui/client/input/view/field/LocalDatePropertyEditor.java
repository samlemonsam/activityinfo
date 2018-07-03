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
package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import org.activityinfo.io.match.date.LatinDateParser;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.util.Date;

public class LocalDatePropertyEditor extends PropertyEditor<Date> {

    public static final LatinDateParser PARSER = new LatinDateParser();
    public static final DateTimeFormat FORMATTER = DateTimeFormat.getFormat("yyyy-MM-dd");

    @Override
    @SuppressWarnings("deprecation")
    public Date parse(CharSequence text) throws ParseException {

        // Use a very lenient parser to allow for local input.
        // The result will always be formatted as a formatted string

        try {
            return PARSER.parse(text.toString()).atMidnightInMyTimezone();
        } catch (IllegalArgumentException e) {
            throw new ParseException(text.toString(), 0);
        }
    }

    @SuppressWarnings("deprecation")
    protected boolean twoDigitYear(@Nonnull Date date) {
        return (date.getYear()+1900) < 100;
    }

    @SuppressWarnings("deprecation")
    protected Date correctTwoDigitYear(Date date) {
        Date defaultCenturyStart = new Date();
        defaultCenturyStart.setYear(defaultCenturyStart.getYear() - 80);

        Date correctedDate = new Date(date.getYear() + 1900, date.getMonth(), date.getDate());
        while (correctedDate.before(defaultCenturyStart)) {
            correctedDate.setYear(correctedDate.getYear() + 100);
        }
        return correctedDate;
    }

    @Override
    public String render(Date date) {
        return FORMATTER.format(date);
    }
}
