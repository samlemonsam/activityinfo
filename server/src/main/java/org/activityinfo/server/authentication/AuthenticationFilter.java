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

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.lightoze.gwt.i18n.server.ThreadLocalLocaleProvider;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.database.hibernate.entity.Authentication;

import javax.persistence.EntityManager;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This filter tries to establish the identify of the connected user at the
 * start of each request.
 * <p/>
 * <p/>
 * If the request is successfully authenticated, it is stored in the
 * {@link ServerSideAuthProvider}.
 */
@Singleton
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    private final Provider<HttpServletRequest> request;
    private final Provider<EntityManager> entityManager;
    private final ServerSideAuthProvider authProvider;
    private final BasicAuthentication basicAuthenticator;

    private final LoadingCache<String, AuthenticatedUser> authTokenCache;

    @Inject
    public AuthenticationFilter(Provider<HttpServletRequest> request,
                                Provider<EntityManager> entityManager,
                                ServerSideAuthProvider authProvider,
                                BasicAuthentication basicAuthenticator) {
        this.entityManager = entityManager;
        this.request = request;
        this.authProvider = authProvider;
        this.basicAuthenticator = basicAuthenticator;
        authTokenCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(6, TimeUnit.HOURS)
            .build(new CacheLoader<String, AuthenticatedUser>() {
                @Override
                public AuthenticatedUser load(String authToken) throws Exception {
                    return queryAuthToken(authToken);
                }
            });
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {

        allowCrossOriginRequests((HttpServletResponse) response);

        authProvider.clear();

        String authToken = ((HttpServletRequest) request).getHeader("Authorization");
        if (Strings.isNullOrEmpty(authToken)) {
            authToken = authTokenFromCookie();
        }
        if (authToken != null) {
            try {
                AuthenticatedUser currentUser = authTokenCache.get(authToken);
                authProvider.set(currentUser);
                if(currentUser != null) {
                    LOGGER.info("Request authenticated as " + currentUser.getEmail());
                }
            } catch (Exception e) {
                authProvider.clear();
            }
        }

        // Unless overridden by the Application page or command handler,
        // we'll keep the default to English. Otherwise you end up with unexpected
        // behavior when the content of a page changes when a cookie is / is not present
        // https://bedatadriven.atlassian.net/browse/AI-753
        
        ThreadLocalLocaleProvider.pushLocale(Locale.ENGLISH);

        try {
            filterChain.doFilter(request, response);
        
        } finally {
            ThreadLocalLocaleProvider.popLocale();
        }
    }

    private void allowCrossOriginRequests(HttpServletResponse response) {

        // Allow all sites to make unauthenticated, cross-origin requests
        response.addHeader("Access-Control-Allow-Origin", "*");

        // cache cor permission for a full day
        response.addHeader("Access-Control-Max-Age", Integer.toString(24 * 60 * 60));
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    private AuthenticatedUser queryAuthToken(String authToken) {
        Authentication entity = entityManager.get().find(Authentication.class, authToken);
        if (entity == null) {
            // try as basic authentication
            entity = basicAuthenticator.tryAuthenticate(authToken);
        }
        if (entity == null) {
            throw new IllegalArgumentException();
        }
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(authToken,
                entity.getUser().getId(),
                entity.getUser().getEmail());
        authenticatedUser.setUserLocale(entity.getUser().getLocale());
        return authenticatedUser;
    }

    private String authTokenFromCookie() {
        Cookie[] cookies = request.get().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AuthenticatedUser.AUTH_TOKEN_COOKIE)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
