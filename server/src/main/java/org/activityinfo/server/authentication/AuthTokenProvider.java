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

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.database.hibernate.dao.AuthenticationDAO;
import org.activityinfo.server.database.hibernate.dao.Transactional;
import org.activityinfo.server.database.hibernate.entity.Authentication;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.AuthTokenEntity;

import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.ws.rs.core.NewCookie;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AuthTokenProvider {

    private static final String ROOT = "/";
    private static final String DOMAIN = "activityinfo.org";
    private static final int THIS_SESSION = -1;
    private static final int ONE_YEAR = 365 * 24 * 60 * 60;

    private final Provider<AuthenticationDAO> authDAO;

    @Inject
    public AuthTokenProvider(Provider<AuthenticationDAO> authDAO) {
        super();
        this.authDAO = authDAO;
    }

    @Transactional
    public Authentication createNewAuthToken(User user) {
        Authentication auth = new Authentication(user);
        authDAO.get().persist(auth);

        // Dual write to HRD
        AuthTokenEntity entity = new AuthTokenEntity();
        entity.setCreationTime(new Date());
        entity.setUserId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setToken(auth.getId());
        entity.setExpireTime(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5)));

        Hrd.ofy().save().entity(entity).now();

        return auth;
    }

    public NewCookie[] createNewAuthCookies(User user, URI baseUri) {
        Authentication token = createNewAuthToken(user);

        NewCookie cookie = newAuthCookie(AuthenticatedUser.AUTH_TOKEN_COOKIE, token.getId(), baseUri);
        NewCookie userCookie = newAuthCookie(AuthenticatedUser.USER_ID_COOKIE,
                Integer.toString(token.getUser().getId()), baseUri);
        NewCookie emailCookie = newAuthCookie(AuthenticatedUser.EMAIL_COOKIE, user.getEmail(), baseUri);

        return new NewCookie[]{cookie, userCookie, emailCookie };
    }

    private NewCookie newAuthCookie(String name, String value, URI baseUri) {
        String path = ROOT;
        String domain = domain(baseUri);
        String comment = null;
        int maxAge = THIS_SESSION;
        boolean onlySecure = DeploymentEnvironment.isAppEngineProduction();
        return new NewCookie(name, value, path, domain, comment, maxAge, onlySecure);
    }

    private static String domain(URI baseUri) {
        // If this is an xxx.activityinfo.org host, set a cross-domain cookie
        if (baseUri.getHost().contains(DOMAIN)) {
            return DOMAIN;
        }
        // If base URI is not an xxx.activityinfo.org host, then set just for this host
        return null;
    }
}
