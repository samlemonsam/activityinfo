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

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;

/**
 * Subclass of GuiceContainer that registers ContainerResponseFilters. 
 */
@Singleton
public class JaxRsContainer extends GuiceContainer {
    /**
     * Creates a new Injector.
     *
     * @param injector the Guice injector
     */
    @Inject
    public JaxRsContainer(Injector injector) {
        super(injector);
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props, WebConfig webConfig) throws ServletException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, CharsetResponseFilter.class.getName());
        
        // Silence errors about not being able to generate WADL from random requests
        parameters.put(ResourceConfig.FEATURE_DISABLE_WADL, true);
        
        DefaultResourceConfig config = new DefaultResourceConfig();
        config.setPropertiesAndFeatures(parameters);
        
        return config;
    }
}
