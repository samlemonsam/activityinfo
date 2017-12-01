package chdc.server;

import com.google.common.collect.Sets;
import org.activityinfo.store.server.JaxRsJsonReader;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Defines the JAX-RS Application
 */
public class ChdcApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(ChdcApplication.class.getName());

    public ChdcApplication() {
        LOGGER.info("Initializing Chdc Application");
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        singletons.add(new JaxRsJsonReader());

        return singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ApiResource.class);
        classes.add(FrontendResource.class);
        return classes;
    }
}
