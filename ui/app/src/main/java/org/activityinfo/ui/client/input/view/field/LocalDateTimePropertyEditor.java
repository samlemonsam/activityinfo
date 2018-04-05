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

import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.util.Date;

public class LocalDateTimePropertyEditor extends PropertyEditor<Date> {

    private DateTimePropertyEditor delegate = new DateTimePropertyEditor();

    /**
     * <p>
     *     If a year pattern does not have exactly two 'y' characters, the year is interpreted literally, regardless of
     *     the number of digits. For example, using the pattern <code>"MM/dd/yyyy"</code>, "01/11/12" parses to
     *     Jan 11, 12 A.D.
     * </p>
     * <p>
     *     Due to variations in locale date patterns ("y" vs. "yy"), non-intuitive behaviour can occur when
     *     entering short dates which are nevertheless in line with {@link DateTimeFormatInfo} implementations. This
     *     causes users of some locales to enter "01/01/20" believing it to be the 1 Jan 2020 C.E., when in fact the
     *     locale year format of 'y' will cause the shortened year to be interpreted as the literal year 20 C.E.
     * </p>
     * <p>
     *     Therefore it is entirely possible for a user to enter a valid two-digit year for their date, depending on
     *     locale. We correct for this by adjusting dates to be within 80 years before and 20 years after the time the
     *     property editor instance is created.
     * </p>
     * <p>
     *     A user with locale year pattern <code>"dd MM y"</code> entering the date <code>"01 01 20"</code> in 1990,
     *     would have their year parsed as 20 C.E. and corrected to 1920 C.E. If the user was to enter the same date in
     *     2015, they would have their year parsed as 20 C.E. and corrected to 2020 C.E.
     * </p>
     * **/
    @Override
    @SuppressWarnings("deprecation")
    public Date parse(CharSequence text) throws ParseException {
        Date date = delegate.parse(text);
        if (twoDigitYear(date)) {
            date = correctTwoDigitYear(date);
        }
        return date;
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
        return delegate.render(date);
    }
}
