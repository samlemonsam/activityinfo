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

import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
public class CreateDatabaseTest extends CommandTestCase {

    @Test
    @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void testCreate() throws CommandException {

        UserDatabaseDTO db = new UserDatabaseDTO();
        db.setName("RIMS");
        db.setFullName("Reintegration Management Information System");

        CreateResult cr = execute(new CreateEntity(db));

        SchemaDTO schema = execute(new GetSchema());

        UserDatabaseDTO newdb = schema.getDatabaseById(cr.getNewId());

        assertNotNull(newdb);
        assertEquals(db.getName(), newdb.getName());
        assertEquals(db.getFullName(), newdb.getFullName());
        assertNotNull(newdb.getCountry());
        assertEquals("Alex", newdb.getOwnerName());
        assertThat(newdb.getPartners(), hasSize(1));
    }

    @Test
    @OnDataSet("/dbunit/multicountry.db.xml")
    public void createWithSpecificCountry() throws CommandException {

        UserDatabaseDTO db = new UserDatabaseDTO();
        db.setName("Warchild Haiti");
        db.setFullName("Warchild Haiti");

        setUser(1);

        CreateEntity cmd = new CreateEntity(db);
        cmd.getProperties().put("countryId", 2);
        CreateResult cr = execute(cmd);

        SchemaDTO schema = execute(new GetSchema());

        UserDatabaseDTO newdb = schema.getDatabaseById(cr.getNewId());

        assertNotNull(newdb);
        assertThat(newdb.getCountry(), is(notNullValue()));
        assertThat(newdb.getCountry().getName(), is(equalTo("Haiti")));
    }

}
