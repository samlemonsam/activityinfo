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

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Decorates all response Content-Types with a character set parameter
 */
public class CharsetResponseFilter implements ContainerResponseFilter {
    
    private static final Logger LOGGER = Logger.getLogger(CharsetResponseFilter.class.getName());
    
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        
        MediaType type = response.getMediaType();

        LOGGER.info("Filtering response: " + type);
        
        if (type != null) {
            if (!type.getParameters().containsKey("charset")) {
                Map<String, String> parameters = new HashMap<>();
                parameters.putAll(type.getParameters());
                parameters.put("charset", "UTF-8");
                
                MediaType typeWithCharset = new MediaType(type.getType(), type.getSubtype(), parameters);
              
                response.getHttpHeaders().putSingle("Content-Type", typeWithCharset);
            }
        }
        return response;
    }
}
