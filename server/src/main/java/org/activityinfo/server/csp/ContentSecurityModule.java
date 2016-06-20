package org.activityinfo.server.csp;

import com.google.inject.servlet.ServletModule;
import org.activityinfo.server.DeploymentEnvironment;

/**
 * Applies a Content Security Policy
 */
public class ContentSecurityModule extends ServletModule {
    @Override
    protected void configureServlets() {
        if(!DeploymentEnvironment.isAppEngineDevelopment()) {
            filter("/*").through(ContentSecurityFilter.class);
            serve("/csp-violation").with(ContentSecurityServlet.class);
        }
    }
}
