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

import com.google.appengine.api.datastore.*;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.authentication.ServerSideAuthProvider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;

public class AuthenticationTokenServiceImpl implements AuthenticationTokenService {

    public static final String KIND = "XFormAuthToken";
    private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
    private ServerSideAuthProvider authProvider;

    @Inject
    public AuthenticationTokenServiceImpl(ServerSideAuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public String createAuthenticationToken(int userId, ResourceId formClassId) {
        String token = "token:" + Long.toHexString(new SecureRandom().nextLong());
        Entity entity = new Entity(key(token));
        entity.setUnindexedProperty("userId", userId);
        entity.setUnindexedProperty("formClassId", formClassId.asString());
        entity.setUnindexedProperty("creationTime", System.currentTimeMillis());
        datastoreService.put(null, entity);

        return token;
    }

    private Key key(String token) {
        return KeyFactory.createKey(KIND, token);
    }

    @Override
    public AuthenticatedUser authenticate(String authenticationToken) {
        Entity authEntity = null;
        try {
            authEntity = datastoreService.get(key(authenticationToken));
        } catch (EntityNotFoundException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        long userId = (Long) authEntity.getProperty("userId");
        AuthenticatedUser user = new AuthenticatedUser("", (int) userId, "@");
        authProvider.set(user);

        return user;
    }
}
