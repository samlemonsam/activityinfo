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

import com.google.common.base.Strings;
import org.realityforge.gwt.appcache.server.AbstractManifestServlet;
import org.realityforge.gwt.appcache.server.BindingProperty;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;
import org.realityforge.gwt.appcache.server.propertyprovider.UserAgentPropertyProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Overrides the behavior of the default gwt-appcache do custom
 * locale selection based on the authenticated user's profile.
 *
 */
public class ManifestServlet extends AbstractManifestServlet {

    private static final Logger LOGGER = Logger.getLogger(ManifestServlet.class.getName());

    private static final long serialVersionUID = 5078231093739821294L;

    public ManifestServlet() {
        super();
        addPropertyProvider(new UserAgentPropertyProvider());
        addPropertyProvider(new LocaleProvider());
    }

    private class LocaleProvider implements PropertyProvider {

        @Nonnull
        @Override
        public String getPropertyName() {
            return "locale";
        }

        @Nullable
        @Override
        public String getPropertyValue(@Nonnull HttpServletRequest request) throws Exception {
            String locale = request.getParameter("locale");
            if (Strings.isNullOrEmpty(locale)) {
                return "en";
            }
            return locale;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("Serving manfiest...");
        super.doGet(request, response);
    }

    @Override
    protected boolean handleUnmatchedRequest(HttpServletRequest request, HttpServletResponse response,
                                             String moduleName, String baseUrl,
                                             List<BindingProperty> computedBindings) throws ServletException, IOException {
        LOGGER.severe(String.format("Failed to match permutation: moduleName = %s, bindings = %s",
                moduleName,
                computedBindings));

        return false;
    }
}
