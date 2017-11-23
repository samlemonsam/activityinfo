package chdc.server;

import org.activityinfo.store.server.JaxRsJsonReader;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines the JAX-RS Application
 */
public class ChdcApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ApiResource.class);
        classes.add(JaxRsJsonReader.class);
        return classes;
    }
}
