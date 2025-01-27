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
package org.activityinfo.model.form;

import org.activityinfo.model.resource.ResourceId;

import java.util.Comparator;

/**
 * @author yuriyz on 01/22/2016.
 */
public class FormInstanceComparators {

    private FormInstanceComparators() {
    }

    public static Comparator<TypedFormRecord> doubleComparator(final ResourceId sortField) {
        return new Comparator<TypedFormRecord>() {
            @Override
            public int compare(TypedFormRecord o1, TypedFormRecord o2) {
                Double d1 = o1.getDouble(sortField);
                Double d2 = o2.getDouble(sortField);
                if (d1 != null && d2 != null) {
                    return d1.compareTo(d2);
                }
                return 0;
            }
        };
    }

    public static Comparator<TypedFormRecord> stringComparator(final ResourceId sortField) {
        return new Comparator<TypedFormRecord>() {
            @Override
            public int compare(TypedFormRecord o1, TypedFormRecord o2) {
                String s1 = o1.getString(sortField);
                String s2 = o2.getString(sortField);
                if (s1 != null && s2 != null) {
                    return s1.compareTo(s2);
                }
                return 0;
            }
        };
    }
}
