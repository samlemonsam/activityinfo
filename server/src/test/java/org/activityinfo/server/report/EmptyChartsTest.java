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
package org.activityinfo.server.report;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetDimensionLabels;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.legacy.shared.reports.model.PivotChartReportElement;
import org.activityinfo.legacy.shared.reports.model.PivotChartReportElement.Type;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.dao.IndicatorDAO;
import org.activityinfo.server.database.hibernate.entity.Indicator;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.report.generator.PivotChartGenerator;
import org.junit.Test;

import java.util.Collections;

import static org.easymock.EasyMock.*;

public class EmptyChartsTest {

    @Test
    public void generate() {
        PivotChartReportElement element = new PivotChartReportElement(
                Type.StackedBar);
        element.setIndicator(1);
        element.addCategoryDimension(new Dimension(DimensionType.Partner));
        element.addSeriesDimension(new Dimension(DimensionType.Database));

        DispatcherSync dispatcher = createMock(DispatcherSync.class);
        expect(dispatcher.execute(isA(PivotSites.class)))
                .andReturn(new PivotSites.PivotResult(Collections.EMPTY_LIST));

        expect(dispatcher.execute(isA(GetDimensionLabels.class)))
                .andReturn(
                        new GetDimensionLabels.DimensionLabels(Collections.EMPTY_MAP));

        replay(dispatcher);

        IndicatorDAO indicatorDAO = createMock(IndicatorDAO.class);
        expect(indicatorDAO.findById(eq(1))).andReturn(new Indicator());
        replay(indicatorDAO);

        PivotChartGenerator generator = new PivotChartGenerator(dispatcher,
                indicatorDAO);
        generator.generate(new User(), element, new Filter(), new DateRange());
    }

}
