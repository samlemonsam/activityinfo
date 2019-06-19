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

import com.bedatadriven.rebar.appcache.server.DefaultSelectionServlet;
import com.bedatadriven.rebar.appcache.server.PropertyProvider;
import com.bedatadriven.rebar.appcache.server.SelectionException;
import com.bedatadriven.rebar.appcache.server.UnknownUserAgentException;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.AuthenticatedUser;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Overrides the behavior of the default rebar-appcache servlet to do custom
 * locale selection based on the authenticated user's profile.
 *
 * @author alex
 */
@Singleton
public class SelectionServlet extends DefaultSelectionServlet {
    private static final long serialVersionUID = 5078231093739821294L;

    @Inject
    public SelectionServlet(Provider<AuthenticatedUser> userProvider) {
        super();
        registerProvider("locale", new LocaleProvider(userProvider));
        registerProvider("gwt.logging.logLevel", new LogLevelProvider());
    }

    private class LocaleProvider implements PropertyProvider {

        private final Provider<AuthenticatedUser> userProvider;

        public LocaleProvider(Provider<AuthenticatedUser> userProvider) {
            this.userProvider = userProvider;
        }

        @Override
        public String get(HttpServletRequest req) {
            AuthenticatedUser user = userProvider.get();
            if(user.isAnonymous()) {
                throw new UserNotAuthenticatedException("expired authtoken");
            }
            return user.getUserLocale();
        }

        private String getAuthToken(HttpServletRequest req) {
            for (Cookie cookie : req.getCookies()) {
                if (cookie.getName().equals(AuthenticatedUser.AUTH_TOKEN_COOKIE)) {
                    return cookie.getValue();
                }
            }
            throw new UserNotAuthenticatedException("No authToken cookie");
        }
    }

    private class UserNotAuthenticatedException extends SelectionException {
        private static final long serialVersionUID = 7911507877946155993L;

        public UserNotAuthenticatedException(String message) {
            super(message);
        }
    }

    @Override
    protected void handleSelectionException(Path path, Exception e, HttpServletResponse resp) throws IOException {
        if (e instanceof UnknownUserAgentException) {
            resp.getWriter().print("window.location = '/app/unsupportedBrowser'; ");
        } else {
            resp.sendError(CACHE_OBSOLETE, e.getMessage());
        }
    }

    @Override
    protected void handleNoAvailablePermutation(Path path, HttpServletResponse resp) throws IOException {
        if (path.fileType.equals("js")) {
            resp.getWriter().println("window.location = '/app/unsupportedBrowser';");
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported browser");
        }
    }

    /**
     * Set the log_level to be used based on the host name
     */
    private class LogLevelProvider implements PropertyProvider {
        @Override
        public String get(HttpServletRequest request) {
            if (request.getServerName().contains("localhost") ||
                request.getServerName().contains("127.0.0.1") ||
                request.getServerName().contains("trace")) {

                return "FINEST";

            } else {
                return "SEVERE";
            }
        }
    }

    @VisibleForTesting void testInit(ServletConfig config) throws ServletException {
        init(config);
    }

    @VisibleForTesting
    void testGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
