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

import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.util.jaxrs.AbstractRestModule;

/**
 * Guice Module defining bindings for Authentication interfaces. (The default is
 * checks the password against the database)
 * <p/>
 * See
 * http://code.google.com/p/google-web-toolkit-incubator/wiki/LoginSecurityFAQ
 * for background
 */
public class AuthenticationModule extends AbstractRestModule {

    @Override
    protected void configureResources() {
        bind(Authenticator.class).to(DatabaseAuthenticator.class);
        bind(AuthenticatedUser.class).toProvider(ServerSideAuthProvider.class);
        filter("/*").through(AuthenticationFilter.class);
        bindResource(HumanitarianId.class);
    }
}
