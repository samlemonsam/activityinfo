package org.activityinfo.server.util.jaxrs;

import com.google.inject.servlet.ServletModule;
import org.activityinfo.store.server.CacheFilterFactory;
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
        bind(JacksonJsonProvider.class).toInstance(new Utf8JacksonJsonProvider());
        bind(CacheFilterFactory.class);
    }
}
