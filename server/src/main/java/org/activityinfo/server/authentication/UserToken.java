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

import java.util.List;

/**
 * @author yuriyz on 10/29/2014.
 */
public class UserToken {

    private String secureToken;
    private long userId;
    private List<UserTokenScope> scopes;

    public UserToken() {
    }

    public UserToken(String secureToken, long userId, List<UserTokenScope> scopes) {
        this.secureToken = secureToken;
        this.userId = userId;
        this.scopes = scopes;
    }

    public String getSecureToken() {
        return secureToken;
    }

    public UserToken setSecureToken(String secureToken) {
        this.secureToken = secureToken;
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public UserToken setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public List<UserTokenScope> getScopes() {
        return scopes;
    }

    public UserToken setScopes(List<UserTokenScope> scopes) {
        this.scopes = scopes;
        return this;
    }
}
