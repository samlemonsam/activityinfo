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
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.database.hibernate.dao.Transactional;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.AuthTokenEntity;

import javax.ws.rs.core.NewCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AuthTokenProvider {

    public static final String DOMAIN = "activityinfo.org";

    private static final String ROOT = "/";
    private static final int THIS_SESSION = -1;
    private static final int ONE_YEAR = 365 * 24 * 60 * 60;
    private static final int MAX_NEW_COOKIES = 6;           // Max is 2 sets of 3 cookies (host domain and cross domain)

    @Transactional
    public String createNewAuthToken(User user) {

        String token = SecureTokenGenerator.generate();

        // Dual write to HRD
        AuthTokenEntity entity = new AuthTokenEntity();
        entity.setCreationTime(new Date());
        entity.setUserId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setToken(token);
        entity.setExpireTime(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5)));

        Hrd.ofy().save().entity(entity).now();

        return token;
    }

    public NewCookie[] createNewAuthCookies(User user, URI baseUri) {
        String token = createNewAuthToken(user);
        List<NewCookie> newCookies = new ArrayList<>(MAX_NEW_COOKIES);

        if (baseUri.getHost().contains(DOMAIN)) {
            // set cross domain cookies
            newCookies.add(newAuthCookie(AuthenticatedUser.AUTH_TOKEN_COOKIE, token, DOMAIN));
            newCookies.add(newAuthCookie(AuthenticatedUser.USER_ID_COOKIE, Integer.toString(user.getId()), DOMAIN));
            newCookies.add(newAuthCookie(AuthenticatedUser.EMAIL_COOKIE, user.getEmail(), DOMAIN));
        }

        // set host domain cookies
        newCookies.add(newAuthCookie(AuthenticatedUser.AUTH_TOKEN_COOKIE, token, null));
        newCookies.add(newAuthCookie(AuthenticatedUser.USER_ID_COOKIE, Integer.toString(user.getId()), null));
        newCookies.add(newAuthCookie(AuthenticatedUser.EMAIL_COOKIE, user.getEmail(), null));

        NewCookie[] newCookieArray = new NewCookie[newCookies.size()];
        return newCookies.toArray(newCookieArray);
    }

    private NewCookie newAuthCookie(String name, String value, String domain) {
        String path = ROOT;
        String comment = null;
        int maxAge = THIS_SESSION;
        boolean onlySecure = DeploymentEnvironment.isAppEngineProduction();
        return new NewCookie(name, value, path, domain, comment, maxAge, onlySecure);
    }

}
