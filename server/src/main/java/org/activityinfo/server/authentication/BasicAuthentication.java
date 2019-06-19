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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.database.hibernate.dao.UserDAO;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.logging.Logger;

public class BasicAuthentication {

    private static final Logger LOGGER = Logger.getLogger(BasicAuthentication.class.getName());

    private final ServerSideAuthProvider authProvider;
    private final Provider<UserDAO> userDAO;
    private final Provider<Authenticator> authenticator;

    @Inject
    public BasicAuthentication(ServerSideAuthProvider authProvider,
                               Provider<UserDAO> userDAO,
                               Provider<Authenticator> authenticator) {
        this.authProvider = authProvider;
        this.userDAO = userDAO;
        this.authenticator = authenticator;
    }

    public User tryAuthenticate(String authorizationHeader) {
        User user;
        try {
            return doAuthentication(authorizationHeader);
        } catch (IOException e) {
            return null;
        }
    }

    public User doAuthentication(String auth) throws IOException {

        User user = authenticate(auth);

        if (user == null) {
            return null;
        }

        authProvider.set(new AuthenticatedUser("", user.getId(), user.getEmail()));

        return user;
    }

    // This method checks the user information sent in the Authorization
    // header against the database of users maintained in the users Hashtable.

    public User authenticate(String auth) {
        if (Strings.isNullOrEmpty(auth)) {
            // no auth
            return null;
        }
        if (!auth.toUpperCase().startsWith("BASIC ")) {

            LOGGER.severe("Unsupported authorization header [" + auth + "]");
            // we only do BASIC
            return null;
        }
        // Get encoded user and password, comes after "BASIC "
        String emailPasswordEncoded = auth.substring(6);

        // Decode it, using any base 64 decoder

        byte[] emailPassDecodedBytes = BaseEncoding.base64().decode(emailPasswordEncoded);
        String emailPassDecoded = new String(emailPassDecodedBytes, Charsets.UTF_8);
        String[] emailPass = emailPassDecoded.split(":");

        if (emailPass.length != 2) {
            return null;
        }

        // look up the user in the database
        User user = null;
        try {
            user = userDAO.get().findUserByEmail(emailPass[0]);
        } catch (NoResultException e) {
            return null;
        }

        if (!authenticator.get().check(user, emailPass[1])) {
            return null;
        }
        return user;

    }
}
