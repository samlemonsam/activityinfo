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
package org.activityinfo.legacy.shared.command;

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class GetDimensionLabelsHandlerTest extends CommandTestCase2 {

    @Test
    public void labelsTest() throws CommandException {
        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Activity,
                Arrays.asList(1, 2, 3, 4, 5));
        filter.addRestriction(DimensionType.AdminLevel,
                Arrays.asList(1, 2, 3, 4, 5));
        filter.addRestriction(DimensionType.Partner,
                Arrays.asList(1, 2, 3, 4, 5));
        filter.addRestriction(DimensionType.Project,
                Arrays.asList(1, 2, 3, 4, 5));
        filter.addRestriction(DimensionType.Database,
                Arrays.asList(1, 2, 3, 4, 5));
        filter.addRestriction(DimensionType.Indicator,
                Arrays.asList(1, 2, 3, 4, 5));

        for (DimensionType dimension : filter.getRestrictedDimensions()) {
            Map<Integer, String> labels = execute(filter, dimension);

            // We don't make assumptions about the data, only about that there
            // should be data (working query)
            assertTrue(
                    "Expected at least one label for entity "
                            + dimension.toString(), labels.size() != 0);
        }
    }

    private Map<Integer, String> execute(Filter filter, DimensionType dimension)
            throws CommandException {
        return execute(
                new GetDimensionLabels(dimension, filter.getRestrictions(dimension)))
                .getLabels();
    }
}
