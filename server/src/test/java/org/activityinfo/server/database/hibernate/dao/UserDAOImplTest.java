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
package org.activityinfo.server.database.hibernate.dao;

import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@Modules({TestHibernateModule.class})
@OnDataSet("/dbunit/schema1.db.xml")
public class UserDAOImplTest {

    @Inject
    private UserDAO userDAO;

    @Test
    public void testDoesUserExist() throws Exception {
        assertTrue(userDAO.doesUserExist("bavon@nrcdrc.org"));
    }

    @Test
    public void testDoesUserExistWhenNoUser() throws Exception {
        assertFalse(userDAO.doesUserExist("nonexistantuser@solidarites.org"));
    }

    @Test
    public void testFindUserByEmail() throws Exception {
        User user = userDAO.findUserByEmail("bavon@nrcdrc.org");

        assertEquals("id", 2, user.getId());
    }

    @Test
    public void testFindUserByChangePasswordKey() throws Exception {
    }
}
