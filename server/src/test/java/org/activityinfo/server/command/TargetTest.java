package org.activityinfo.server.command;

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
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.TargetDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/schema1.db.xml")
public class TargetTest extends CommandTestCase {

    private static final int DATABASE_OWNER = 1;
    private static UserDatabaseDTO db;

    @Before
    public void setUser() {
        setUser(DATABASE_OWNER);

        SchemaDTO schema = execute(new GetSchema());
        db = schema.getDatabaseById(1);
    }

    @Test
    public void testTarget() throws CommandException {

        TargetDTO target = createTarget();

        CreateResult cresult = execute(new AddTarget(db.getId(), target));

        int newId = cresult.getNewId();

        List<TargetDTO> targets = execute(new GetTargets(db.getId())).getData();

        TargetDTO dto = getTargetById(targets, newId);

        assertNotNull(dto);
        assertEquals("name", "Target0071", dto.getName());
    }

    @Test
    public void updateTarget() throws Throwable {

        LocalDate toDate = new LocalDate(2015, 3, 3);
        LocalDate fromDate = new LocalDate(2015, 3, 4);

        Map<String, Object> changes = new HashMap<String, Object>();
        changes.put("name", "newNameOfTarget");
        changes.put("toDate", toDate);
        changes.put("fromDate", fromDate);

        execute(new BatchCommand(new UpdateEntity("Target", 1, changes)));

        List<TargetDTO> targets = execute(new GetTargets(db.getId())).getData();

        TargetDTO dto = getTargetById(targets, 1);

        assertEquals("newNameOfTarget", dto.getName());
        assertEquals(fromDate, dto.getFromDate());
        assertEquals(toDate, dto.getToDate());
    }

    @Test
    public void deleteTargetTest() {

        TargetDTO target = createTarget();

        CreateResult cresult = execute(new AddTarget(db.getId(), target));

        int newId = cresult.getNewId();

        List<TargetDTO> targets = execute(new GetTargets(db.getId())).getData();

        TargetDTO dto = getTargetById(targets, newId);

        assertEquals("name", "Target0071", dto.getName());

        execute(new Delete(dto));

        targets = execute(new GetTargets()).getData();

        TargetDTO deleted = getTargetById(targets, newId);

        assertNull(deleted);
    }

    private TargetDTO createTarget() {
        Date date1 = new Date();
        Date date2 = new Date();

        TargetDTO target = new TargetDTO();
        target.setName("Target0071");
        target.setFromDate(date1);
        target.setToDate(date2);

        return target;
    }

    private TargetDTO getTargetById(List<TargetDTO> targets, int id) {
        for (TargetDTO dto : targets) {
            if (id == dto.getId()) {
                return dto;
            }
        }

        return null;
    }
}
