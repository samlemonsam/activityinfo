package chdc.server;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class ChdcDatabaseFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(ChdcDatabaseFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
            ChdcDatabase.commitRequestTransactionIfActive();

        } catch (Exception e) {
            LOGGER.info("Request failed, rolling back transaction");
            ChdcDatabase.rollbackRequestTransactionIfActive();
        }
    }

    @Override
    public void destroy() {
    }
}
