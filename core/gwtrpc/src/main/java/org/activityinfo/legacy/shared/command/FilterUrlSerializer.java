package org.activityinfo.legacy.shared.command;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.activityinfo.legacy.shared.reports.model.DateRange;

import java.util.Set;

/**
 * Serializes/deserializes Filter's fit for use in URLs in the format:
 * <p/>
 * <pre>
 * activity + 13 + 14 - database + 13 - date + 2012 + 20
 * </pre>
 */
public class FilterUrlSerializer {

    private static final String NULL_DATE = "00000000";

    public static String toUrlFragment(Filter filter) {
        StringBuilder url = new StringBuilder();
        for (DimensionType dim : filter.getRestrictedDimensions()) {
            Set<Integer> ids = filter.getRestrictions(dim);
            if (url.length() > 0) {
                url.append("-");
            }
            url.append(dim.name());
            for (Integer id : ids) {
                url.append("+");
                url.append(id);
            }
        }
        appendDateRange("startDate", filter.getStartDateRange(), url);
        appendDateRange("endDate", filter.getEndDateRange(), url);

        return url.toString();
    }

    private static void appendDateRange(final String fieldName, DateRange range, StringBuilder url) {
        if (range.isRestricted()) {
            if (url.length() > 0) {
                url.append("-");
            }
            url.append(fieldName + "+");
            appendDate(range.getMinLocalDate(), url);
            url.append('+');
            appendDate(range.getMaxLocalDate(), url);
        }
    }

    public static Filter fromUrlFragment(String fragment) {
        Filter filter = new Filter();
        String[] dimensions = fragment.split("\\-");

        for (String dim : dimensions) {
            String[] elements = dim.split("\\+");
            if (elements[0].equals("date") || elements[0].equals("endDate")) {
                filter.getEndDateRange().setMinDate(parseDate(elements[1]));
                filter.getEndDateRange().setMaxDate(parseDate(elements[2]));
            } else if(elements[0].equals("startDate")) {
                filter.getStartDateRange().setMinDate(parseDate(elements[1]));
                filter.getStartDateRange().setMaxDate(parseDate(elements[2]));                
            } else {
                DimensionType dimType = DimensionType.valueOf(elements[0]);
                for (int i = 1; i < elements.length; ++i) {
                    filter.addRestriction(dimType, Integer.parseInt(elements[i]));
                }
            }
        }
        return filter;
    }

    public static Filter fromQueryParameter(String value) {
        return fromUrlFragment(value.replace(' ', '+'));
    }

    private static LocalDate parseDate(String string) {
        return new LocalDate(Integer.parseInt(string.substring(0, 4)),
                Integer.parseInt(string.substring(4, 6)),
                Integer.parseInt(string.substring(6, 8)));
    }

    private static void appendDate(LocalDate date, StringBuilder sb) {
        if (date == null) {
            sb.append(NULL_DATE);
        } else {
            sb.append(date.getYear());
            if (date.getMonthOfYear() < 10) {
                sb.append("0");
            }
            sb.append(date.getMonthOfYear());
            if (date.getDayOfMonth() < 10) {
                sb.append("0");
            }
            sb.append(date.getDayOfMonth());
        }
    }
}
