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

import com.google.api.client.util.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author yuriyz on 10/29/2014.
 */
public enum UserTokenScope {

    SUBSCRIBE,
    UNSUBSCRIBE;

    private static final Logger LOGGER = Logger.getLogger(UserTokenScope.class.getName());

    public String getValue() {
        return name();
    }

    public static boolean hasScope(UserTokenScope scope, String scopeString) {
        return parseScopes(scopeString).contains(scope);
    }

    public static List<UserTokenScope> parseScopes(String scopeString) {
        List<UserTokenScope> scopes = Lists.newArrayList();
        String[] split = scopeString.split(" ");
        for (String s : split) {
            UserTokenScope userTokenScope = parseSilently(s);
            if (userTokenScope != null) {
                scopes.add(userTokenScope);
            }
        }

        return scopes;
    }

    private static UserTokenScope parseSilently(String scopeString) {
        try {
            return valueOf(scopeString);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse string: " + scopeString + ", error: " + e.getMessage(), e);
            return null;
        }
    }

    public static String scope(UserTokenScope... scopes) {
        return scope(Arrays.asList(scopes));
    }

    public static String scope(List<UserTokenScope> scopes) {
        String result = "";
        for (UserTokenScope scope : scopes) {
            result = result + scope.getValue() + " ";
        }
        return result.trim();
    }
}
