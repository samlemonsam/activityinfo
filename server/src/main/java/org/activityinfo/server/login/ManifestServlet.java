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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.realityforge.gwt.appcache.server.AbstractManifestServlet;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;
import org.realityforge.gwt.appcache.server.propertyprovider.UserAgentPropertyProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

/**
 * Overrides the behavior of the default rebar-appcache servlet to do custom
 * locale selection based on the authenticated user's profile.
 *
 * @author alex
 */
@Singleton
public class ManifestServlet extends AbstractManifestServlet {
    private static final long serialVersionUID = 5078231093739821294L;

    @Inject
    public ManifestServlet(Provider<EntityManager> entityManager) {
        super();
        addPropertyProvider(new UserAgentPropertyProvider());
        addPropertyProvider(new LocaleProvider(entityManager));
    }

    private class LocaleProvider implements PropertyProvider {

        private final Provider<EntityManager> entityManager;

        public LocaleProvider(Provider<EntityManager> entityManager) {
            this.entityManager = entityManager;
        }

        @Nonnull
        @Override
        public String getPropertyName() {
            return "locale";
        }

        @Nullable
        @Override
        public String getPropertyValue(@Nonnull HttpServletRequest request) throws Exception {
            return localeFromPath(request.getRequestURI());

        }
    }

    @VisibleForTesting
    static String localeFromPath(String path) {
        // The request should be in the form
        //    /{module}/{locale}.appcache
        // For example:
        //    /ActivityInfo/en.appcache
        //    /ActivityInfo/fr.appcache

        if(path.endsWith(".appcache") && path.length() > ".appcache".length() + 2) {
            int lastSlash = path.lastIndexOf('/');
            String locale = path.substring(lastSlash + 1, lastSlash + 3);
            return locale;
        }

        return "en";
    }
}
