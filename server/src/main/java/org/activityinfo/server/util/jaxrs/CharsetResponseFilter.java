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
