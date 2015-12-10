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

import junit.framework.Assert;
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

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/schema1.db.xml")
public class TargetTest extends CommandTestCase {

    private static final int DATABASE_OWNER = 1;
    private static UserDatabaseDTO db;

    @Before
    public void setUser() {
        setUser(DATABASE_OWNER);
        /*
         * Initial data load
         */

        SchemaDTO schema = execute(new GetSchema());
        db = schema.getDatabaseById(1);
    }

    @Test
    public void testTarget() throws CommandException {

        /*
         * Create a new Target
         */

        TargetDTO target = createTarget();

        CreateResult cresult = execute(new AddTarget(db.getId(), target));

        int newId = cresult.getNewId();

        /*
         * Load Targets to verify the changes have stuck
         */

        List<TargetDTO> targets = execute(new GetTargets(db.getId())).getData();

        TargetDTO dto = getTargetById(targets, newId);

        Assert.assertNotNull(dto);
        Assert.assertEquals("name", "Target0071", dto.getName());
    }

    @Test
    public void updateTargetNameTest() throws Throwable {

        Map<String, Object> changes1 = new HashMap<String, Object>();
        changes1.put("name", "newNameOfTarget");

        execute(new BatchCommand(new UpdateEntity("Target", 1, changes1)));

        List<TargetDTO> targets = execute(new GetTargets(db.getId())).getData();

        TargetDTO dto = getTargetById(targets, 1);

        Assert.assertEquals("newNameOfTarget", dto.getName());
    }

    @Test
    public void deleteTargetTest() {

        TargetDTO target = createTarget();

        CreateResult cresult = execute(new AddTarget(db.getId(), target));

        int newId = cresult.getNewId();

        /*
         * Load Targets to verify the changes have stuck
         */

        List<TargetDTO> targets = execute(new GetTargets(db.getId())).getData();

        TargetDTO dto = getTargetById(targets, newId);

        Assert.assertEquals("name", "Target0071", dto.getName());

        /*
         * Delete new target now
         */

        execute(new Delete(dto));

        /*
         * Verify if target is deleted.
         */

        targets = execute(new GetTargets()).getData();

        TargetDTO deleted = getTargetById(targets, newId);

        Assert.assertNull(deleted);

    }

    private TargetDTO createTarget() {
        Date date1 = new Date();
        Date date2 = new Date();
        /*
         * Create a new Target
         */

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
