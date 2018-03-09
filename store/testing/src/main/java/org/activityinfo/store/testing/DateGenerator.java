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
package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Random;

public class DateGenerator implements Supplier<FieldValue> {

    private double missingProbability;
    private final Random random;
    private int minYear;
    private int maxYear;

    public DateGenerator(FormField field, int minYear, int maxYear) {
        this.missingProbability = field.isRequired() ? 0.0 : 0.25;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.random = new Random(67680L);
    }

    public DateGenerator(FormField field) {
        this(field, 1995, 1995+50);
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < missingProbability) {
            return null;
        }
        int year = minYear + random.nextInt(maxYear - minYear + 1);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);

        return new LocalDate(year, month, day);
    }
}
