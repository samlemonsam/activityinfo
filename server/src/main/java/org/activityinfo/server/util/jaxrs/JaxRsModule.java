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
package org.activityinfo.server.util.jaxrs;

import com.google.inject.servlet.ServletModule;
import org.activityinfo.server.endpoint.rest.CacheFilterFactory;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 * Provides the basic configuration for the Jersey Container,
 * including the FreeMarker ViewProcessor and the
 * Jackson Json Provider.
 */
public class JaxRsModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(FreemarkerViewProcessor.class);
        bind(Domain.class).toProvider(DomainProvider.class);
        bind(JacksonJsonProvider.class).toInstance(new Utf8JacksonJsonProvider());
        bind(CacheFilterFactory.class);
    }
}
