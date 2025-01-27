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
import org.activityinfo.fixtures.Modules;
import org.activityinfo.legacy.shared.command.result.ReportVisibilityResult;
import org.activityinfo.legacy.shared.command.result.ReportsResult;
import org.activityinfo.legacy.shared.model.ReportMetadataDTO;
import org.activityinfo.legacy.shared.model.ReportVisibilityDTO;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.report.ReportModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@Modules(ReportModule.class)
@OnDataSet("/dbunit/schema1.db.xml")
public class ReportVisibilityTest extends CommandTestCase2 {

    @Test
    public void update() {

        setUser(1);

        ReportVisibilityDTO db1 = new ReportVisibilityDTO();
        db1.setDatabaseId(1);
        db1.setVisible(true);
        db1.setDefaultDashboard(true);

        ReportVisibilityDTO db2 = new ReportVisibilityDTO();
        db2.setDatabaseId(2);
        db2.setVisible(false);

        ReportVisibilityDTO db3 = new ReportVisibilityDTO();
        db3.setDatabaseId(3);
        db3.setVisible(true);

        UpdateReportVisibility update = new UpdateReportVisibility(1,
                Arrays.asList(db1, db2, db3));
        execute(update);

        ReportVisibilityResult result = execute(new GetReportVisibility(1));
        assertThat(result.getList().size(), equalTo(2));

        // make sure we can still see the report

        ReportsResult visibleToMe = execute(new GetReports());
        assertThat(visibleToMe.getData().size(), equalTo(1));

        setUser(2); // Bavon

        ReportsResult visibleToBavon = execute(new GetReports());
        assertThat(visibleToBavon.getData().size(), equalTo(2));
        assertThat(getById(visibleToBavon, 1).isDashboard(), equalTo(true));
        assertThat(getById(visibleToBavon, 2).isDashboard(), equalTo(false));

        setUser(3); // Stefan, no access to db

        ReportsResult visibleToStefan = execute(new GetReports());
        assertThat(visibleToStefan.getData().size(), equalTo(0));

    }

    private ReportMetadataDTO getById(ReportsResult result, int id) {
        for (ReportMetadataDTO dto : result.getData()) {
            if (dto.getId() == id) {
                return dto;
            }
        }
        throw new AssertionError("no report with id " + id);
    }
}
