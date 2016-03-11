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
