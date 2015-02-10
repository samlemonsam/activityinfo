package org.activityinfo.test.driver;

import com.google.inject.AbstractModule;

/**
 * Created by alex on 9-2-15.
 */
public class DriverModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApplicationDriver.class).to(UiApplicationDriver.class);
    }
}
