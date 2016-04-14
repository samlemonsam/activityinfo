package org.activityinfo.server.csp;

import com.google.inject.servlet.ServletModule;

/**
 * Applies a Content Security Policy
 */
public class ContentSecurityModule extends ServletModule {
    @Override
    protected void configureServlets() {
        filter("/*").through(ContentSecurityFilter.class);
        serve("/csp-violation").with(ContentSecurityServlet.class);
    }
}
