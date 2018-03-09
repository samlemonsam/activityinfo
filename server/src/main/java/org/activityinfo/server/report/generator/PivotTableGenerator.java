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

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.reports.content.PivotContent;
import org.activityinfo.legacy.shared.reports.content.PivotTableData;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.legacy.shared.reports.model.PivotTableReportElement;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.User;

public class PivotTableGenerator extends PivotGenerator<PivotTableReportElement> {

    @Inject
    public PivotTableGenerator(DispatcherSync dispatcher) {
        super(dispatcher);
    }

    @Override
    public void generate(User user, PivotTableReportElement element, Filter inheritedFilter, DateRange dateRange) {

        Filter filter = GeneratorUtils.resolveElementFilter(element, dateRange);
        Filter effectiveFilter = inheritedFilter == null ? filter : new Filter(inheritedFilter, filter);

        PivotTableData data = generateData(user.getId(),
                user.getLocaleObject(),
                element,
                effectiveFilter,
                element.getRowDimensions(),
                element.getColumnDimensions());

        PivotContent content = new PivotContent();
        content.setEffectiveFilter(effectiveFilter);
        content.setFilterDescriptions(generateFilterDescriptions(filter, element.allDimensionTypes(), user));
        content.setData(data);
        element.setContent(content);
    }
}
