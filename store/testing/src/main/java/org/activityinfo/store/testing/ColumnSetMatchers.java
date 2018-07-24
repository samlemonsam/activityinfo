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

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ColumnSetMatchers {

    private static abstract class ColumnViewMatcher extends TypeSafeMatcher<ColumnView> {

        @Override
        protected void describeMismatchSafely(ColumnView item, Description mismatchDescription) {
            mismatchDescription.appendText("was [");
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < item.numRows(); i++) {
                values.add(item.get(i));
            }
            mismatchDescription.appendValueList("[", ", ", "]", values);
        }
    }

    public static TypeSafeMatcher<ColumnView> hasValues(final boolean... values) {
        return new ColumnViewMatcher() {
            @Override
            protected boolean matchesSafely(ColumnView item) {
                if (item.numRows() != values.length) {
                    return false;
                }
                for (int i = 0; i != values.length; ++i) {
                    if (item.getBoolean(i) != (values[i] ? 1 : 0)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("column with values").appendValueList("[", ", ", "]", values);
            }
        };
    }

    public static TypeSafeMatcher<ColumnView> hasValues(final String... values) {
        return new ColumnViewMatcher() {
            @Override
            protected boolean matchesSafely(ColumnView item) {
                if (item.numRows() != values.length) {
                    return false;
                }
                for (int i = 0; i != values.length; ++i) {
                    if (!Objects.equals(item.getString(i), values[i])) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("column with values").appendValueList("[", ", ", "]", values);
            }

        };
    }

    public static TypeSafeMatcher<ColumnView> hasAllNullValuesWithLengthOf(final int length) {
        return new ColumnViewMatcher() {
            @Override
            protected boolean matchesSafely(ColumnView item) {
                if (item.numRows() != length) {
                    return false;
                }
                for (int i = 0; i != length; ++i) {
                    if (item.get(i) != null) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("column with " + length + " null values");
            }
        };
    }

    public static TypeSafeMatcher<ColumnView> hasValues(ResourceId... values) {
        String[] strings = new String[values.length];
        for (int i = 0; i != strings.length; ++i) {
            strings[i] = values[i].toString();
        }
        return hasValues(strings);
    }

    public static ColumnViewMatcher hasValues(final Number... values) {
        return new ColumnViewMatcher() {
            @Override
            protected boolean matchesSafely(ColumnView item) {
                if (item.numRows() != values.length) {
                    return false;
                }
                for (int i = 0; i != values.length; ++i) {
                    if (values[i] == null) {
                        if (!Double.isNaN(item.getDouble(i))) {
                            return false;
                        }
                    } else {
                        return values[i].doubleValue() == item.getDouble(i);
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("column with values").appendValueList("[", ", ", "]", values);
            }
        };
    }
}
