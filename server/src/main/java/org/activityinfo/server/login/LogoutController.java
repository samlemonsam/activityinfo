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
package org.activityinfo.server.login;

import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.authentication.AuthTokenProvider;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path(LogoutController.ENDPOINT)
public class LogoutController {

    public static final String ENDPOINT = "/logout";

    private static final int DEFAULT_MAX_AGE = -1;

    private static final int COOKIES_TO_CLEAR = 8;          // 2 sets of 4 cookies (for cross domain and host domain)

    @GET
    public Response logout(@Context UriInfo uri) throws ServletException, IOException {
        return Response.seeOther(uri.getAbsolutePathBuilder().replacePath(LoginController.ENDPOINT).build())
                       .cookie(emptyCookies(uri.getBaseUri()))
                       .build();
    }

    private NewCookie[] emptyCookies(URI baseUri) {
        List<NewCookie> cookies = new ArrayList<>(COOKIES_TO_CLEAR);

        // clear cross-domain cookies
        if (baseUri.getHost().contains(AuthTokenProvider.DOMAIN)) {
            cookies.add(newEmptyCookie(AuthenticatedUser.AUTH_TOKEN_COOKIE, AuthTokenProvider.DOMAIN));
            cookies.add(newEmptyCookie(AuthenticatedUser.EMAIL_COOKIE, AuthTokenProvider.DOMAIN));
            cookies.add(newEmptyCookie(AuthenticatedUser.USER_ID_COOKIE, AuthTokenProvider.DOMAIN));
            cookies.add(newEmptyCookie(AuthenticatedUser.USER_LOCAL_COOKIE, AuthTokenProvider.DOMAIN));
        }

        // clear host domain cookies
        cookies.add(newEmptyCookie(AuthenticatedUser.AUTH_TOKEN_COOKIE, null));
        cookies.add(newEmptyCookie(AuthenticatedUser.EMAIL_COOKIE, null));
        cookies.add(newEmptyCookie(AuthenticatedUser.USER_ID_COOKIE, null));
        cookies.add(newEmptyCookie(AuthenticatedUser.USER_LOCAL_COOKIE, null));

        NewCookie[] cookieArray = new NewCookie[cookies.size()];
        return cookies.toArray(cookieArray);
    }

    private NewCookie newEmptyCookie(String name, String domain) {
        return new NewCookie(name, null, null, domain, null, DEFAULT_MAX_AGE, false);
    }

}
