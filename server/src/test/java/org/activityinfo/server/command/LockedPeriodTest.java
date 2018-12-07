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
package org.activityinfo.server.command;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.util.Providers;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.CreateLockedPeriod;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.UpdateEntity;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO;
import org.activityinfo.legacy.shared.model.LockedPeriodSet;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.model.database.RecordLock;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.store.spi.DatabaseProvider;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@OnDataSet("/dbunit/sites-simple1.db.xml")
@RunWith(InjectionSupport.class)
public class LockedPeriodTest extends CommandTestCase {


    @Inject
    private BillingAccountOracle billingOracle;


    @Test
    public void createTest() throws CommandException {

        setUser(1);

        LockedPeriodDTO dto = new LockedPeriodDTO();
        dto.setName("my name");
        dto.setFromDate(new LocalDate(2011, 1, 1));
        dto.setToDate(new LocalDate(2011, 1, 31));
        dto.setEnabled(true);

        CreateLockedPeriod create = new CreateLockedPeriod(dto);
        create.setDatabaseId(1);

        CreateResult result = execute(create);

        Map<String, Object> changes = Maps.newHashMap();
        changes.put("toDate", new LocalDate(2011, 2, 28));

        execute(new UpdateEntity("LockedPeriod", result.getNewId(), changes));
    }

    @Test
    public void createForFolder() {

        setUser(1);

        LockedPeriodDTO dto = new LockedPeriodDTO();
        dto.setName("NFI ");
        dto.setFromDate(new LocalDate(2011, 1, 1));
        dto.setToDate(new LocalDate(2011, 1, 31));
        dto.setEnabled(true);

        CreateLockedPeriod create = new CreateLockedPeriod(dto);
        create.setFolderId(1);

        execute(create);

        // Now verify that they appear

        SchemaDTO schema = execute(new GetSchema());

        assertThat(schema.getDatabaseById(1).getFolderById(1).getLockedPeriods(), Matchers.hasSize(1));

        LockedPeriodSet locks = new LockedPeriodSet(schema);

        assertTrue(locks.isActivityLocked(1, new LocalDate(2011, 1,1)));

        // Verify that the new code works too...
        DatabaseProvider provider = injector.getInstance(DatabaseProvider.class);
        UserDatabaseMeta metadata = provider.getDatabaseMetadata(CuidAdapter.databaseId(1), 1).get();

        ArrayList<RecordLock> folderLocks = Lists.newArrayList(metadata.getEffectiveLocks(CuidAdapter.folderId(1)));
        ArrayList<RecordLock> formLocks = Lists.newArrayList(metadata.getEffectiveLocks(CuidAdapter.activityFormClass(1)));

        assertThat(folderLocks, Matchers.hasSize(2));
        assertThat(formLocks, Matchers.hasSize(3));
    }
}
