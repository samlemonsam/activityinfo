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
package org.activityinfo.server.authentication;

import com.google.inject.AbstractModule;
import org.activityinfo.legacy.shared.AuthenticatedUser;

public class AuthenticationModuleStub extends AbstractModule {

    public static ServerSideAuthProvider authProvider = new ServerSideAuthProvider();

    public static void setUserId(int userId) {
        System.out.println("Setting user to id=" + userId);
        switch (userId) {
            case 0:
                authProvider.set(AuthenticatedUser.getAnonymous());
                break;
            default:
                authProvider.set(new AuthenticatedUser("XYZ123", userId, "test@test.com"));
        }
    }

    static {
        setUserId(1);
    }

    public static AuthenticatedUser getCurrentUser() {
        return authProvider.get();
    }

    @Override
    protected void configure() {
        bind(AuthenticatedUser.class).toProvider(authProvider);
    }
}
