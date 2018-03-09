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
package org.activityinfo.server.endpoint.odk;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

public class OdkAuthProvider implements Provider<AuthenticatedUser> {

    private static final Logger LOGGER = Logger.getLogger(OdkAuthProvider.class.getName());

    public static final String DEBUG_USER_ID = "odk.debug.authorization.userid";


    private final ServerSideAuthProvider authProvider;
    private final Provider<EntityManager> em;
    private final DeploymentConfiguration config;

    @Inject
    public OdkAuthProvider(DeploymentConfiguration config, ServerSideAuthProvider authProvider, Provider<EntityManager> em) {
        this.authProvider = authProvider;
        this.em = em;
        this.config = config;
    }

    /**
     * Returns the currently authenticated user. During develop
     *
     *
     *
     * @return the currently authenticated user
     * @throws javax.ws.rs.WebApplicationException if the request is not authenticated
     *
     */
    @Override
    public AuthenticatedUser get() {
        if (authProvider.get().isAnonymous()) {
            if(DeploymentEnvironment.isAppEngineDevelopment()) {
                // do we have a dummy user id configured?
                String odkDebugAuthorizationUserId = config.getProperty(DEBUG_USER_ID);
                if (odkDebugAuthorizationUserId != null) {
                    int userId = Integer.parseInt(odkDebugAuthorizationUserId);
                    if (userId > 0) {
                        // if so, we're assuming that user is authorized.
                        User user = em.get().find(User.class, userId);
                        if (user == null) {
                            throw new IllegalStateException("odk.debug.authorization.userid set, but user cannot be found");
                        }
                        authProvider.set(user);
                        return new AuthenticatedUser("", user.getId(), user.getEmail(), user.getLocale());
                    }
                }
            }
            // otherwise ask for (basic) authentication
            // ODK supports two forms of authentication: BASIC and DIGEST
            // We do not support DIGEST authentication because it would require storing passwords in plaintext or
            // with a weak level of hashing.
            // We support BASIC authentication, but ODK (correctly) will refuse to send passwords in plaintext over a
            // non-https connection. This is desirable in production, but it makes it difficult to test against
            // the local development server which does not support https. As a workaround, we will ONLY support a dummy
            // form of digest authentication when running in development mode.
            if(DeploymentEnvironment.isAppEngineDevelopment()) {
                LOGGER.info("Requested digest authentication for ODK testing purposes.");
                throw new WebApplicationException(Response
                        .status(401)
                        .header("WWW-Authenticate", 
                                "Digest realm=\"Activityinfo\" qop=\"auth\" nonce=\"XYZ\" opaque=\"opaque\"").build());
            } else {
                throw new WebApplicationException(Response
                        .status(401)
                        .header("WWW-Authenticate", "Basic realm=\"Activityinfo\"").build());
            }
        } else {
            // authorized user, continue
            return authProvider.get();
        }
    }
}
