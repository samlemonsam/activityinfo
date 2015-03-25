package org.activityinfo.server.util.monitoring;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ensures that the {@code MetricsReporter} is flushed at the end of each request
 */
@Singleton
public class MetricsRequestFilter implements Filter {
    
    private static final Logger LOGGER = Logger.getLogger(MetricsRequestFilter.class.getName());
    
    private final MetricsReporter reporter;

    @Inject
    public MetricsRequestFilter(MetricsReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            tryFlush();
        }
    }
    
    @Override
    public void destroy() {
        LOGGER.log(Level.INFO, MetricsRequestFilter.class.getName() + ".destroy() called");
        tryFlush();
    }


    private void tryFlush() {
        try {
            reporter.flush();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Metrics reporter failed to flush", e);
        }
    }
}
