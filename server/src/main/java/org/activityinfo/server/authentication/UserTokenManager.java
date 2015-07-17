package org.activityinfo.server.authentication;
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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.Arrays;

/**
 * @author yuriyz on 10/29/2014.
 */
public class UserTokenManager {

    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public static void put(UserToken user) {
        Entity entity = new Entity(KeyFactory.createKey("token", user.getSecureToken()));
        entity.setUnindexedProperty("userId", user.getUserId());
        entity.setUnindexedProperty("scope", UserTokenScope.scope(user.getScopes()));

        datastore.put(null, entity);  // we have a single put to make, no need for transaction
    }

    public static UserToken get(String secureToken) throws EntityNotFoundException {
        Entity entity = datastore.get(KeyFactory.createKey("Token", secureToken));
        return new UserToken()
                .setUserId((Long)entity.getProperty("userId"))
                .setSecureToken(secureToken)
                .setScopes(UserTokenScope.parseScopes((String) entity.getProperty("scope")));
    }

    public static UserToken create(long userId, UserTokenScope... scopes) {
        UserToken user = new UserToken()
                .setSecureToken(SecureTokenGenerator.generate())
                .setUserId(userId)
                .setScopes(Arrays.asList(scopes));
        put(user);
        return user;
    }
}
