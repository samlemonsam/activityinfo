package org.activityinfo.test.driver;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Configures the ApiApplicationDriver 
 */
public class ApiModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(ApplicationDriver.class).to(ApiApplicationDriver.class);
    }
}
