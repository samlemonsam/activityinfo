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
package org.activityinfo.server.report.generator;

import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.legacy.shared.reports.model.ReportElement;

public class GeneratorUtils {

    /**
     * Resolves an element's filter into a the effective filter, taking into
     * account inherited restrictions and the overall <code>DateRange</code> of
     * the report.
     * <p/>
     * Interaction between the report's date range <code>DateRange</code> and
     * the element's filter is specified in
     * {@link org.activityinfo.legacy.shared.reports.model.ReportElement#getFilter()}
     *
     * @param element         The report element for which to resolve the filter
     * @param inheritedFilter The <code>Filter</code> that is inherited from the enclosing
     *                        <code>Report</code> or other container
     * @param dateRange       The overall <code>DateRange</code> of the report. This may be
     *                        <code>null</code>, for example if generation is not occuring
     *                        in the context of an individual element.
     * @return the effective <code>Filter</code>
     */
    public static Filter resolveEffectiveFilter(ReportElement element, Filter inheritedFilter, DateRange dateRange) {

        if (inheritedFilter != null) {
            new Filter(element.getFilter(), inheritedFilter);
        } else {
            new Filter(element.getFilter());
        }
        return resolveElementFilter(element, dateRange);
    }

    public static Filter resolveElementFilter(ReportElement element, DateRange dateRange) {

        Filter filter = new Filter(element.getFilter());

        if (dateRange != null) {
            if (filter.getEndDateRange().getMinDate() == null) {
                filter.getEndDateRange().setMinDate(dateRange.getMinDate());
            }
            if (filter.getEndDateRange().getMaxDate() == null) {
                filter.getEndDateRange().setMaxDate(dateRange.getMaxDate());
            }
        }
        return filter;
    }

}
