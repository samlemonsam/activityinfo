package org.activityinfo.server.csp;

import com.google.inject.Singleton;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Applies Content-Security-Policy headers to all responses
 */
@Singleton
public class ContentSecurityFilter implements Filter {
    
    private ContentSecurityPolicy policy;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        policy = new ContentSecurityPolicy();   
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(servletResponse instanceof HttpServletResponse) {
            policy.applyTo((HttpServletResponse) servletResponse);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
