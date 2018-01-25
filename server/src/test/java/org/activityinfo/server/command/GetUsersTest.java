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

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.GetUsers;
import org.activityinfo.legacy.shared.command.result.UserResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class GetUsersTest extends CommandTestCase {

    private final static int DATABASE_OWNER = 1;

    @Test
    public void testUnsorted() throws CommandException {
        setUser(DATABASE_OWNER);

        GetUsers cmd = new GetUsers(1);
        UserResult result = execute(cmd);

        assertThat(result.getData(), hasSize(3));
    }

    /**
     * Verify that users with ManageUsers permission can get a list of users
     * within their organisation
     */
    @Test
    public void testManageUsersPermission() throws CommandException {
        // populate with a known state and authenticate as user 3, who
        // has ManageUser permissions for Solidarites
        setUser(3); // Lisa from Solidarites

        // execute
        UserResult result = execute(new GetUsers(1));

        // VERIFY that we have 1 result:
        // - the one other solidarites user

        assertThat(result.getTotalLength(), equalTo(1));

        UserPermissionDTO marlene = result.getData().get(0);
        assertThat(marlene.getName(), equalTo("Marlene"));
        assertThat(marlene.hasFolderLimitation(), equalTo(false));
    }

    @Test
    public void testManageAllUsersPermission() throws CommandException {

        setUser(2); // Bavon from NRC(with manageAllUsers) permission

        // execute
        UserResult result = execute(new GetUsers(1));

        // VERIFY that we can get can see the two other users from NRC
        assertThat(result.getTotalLength(), equalTo(2));
    }
}
