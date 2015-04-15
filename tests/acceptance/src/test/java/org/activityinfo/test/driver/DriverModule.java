package org.activityinfo.test.driver;

import com.google.inject.AbstractModule;
import org.activityinfo.test.config.ConfigurationError;

public class DriverModule extends AbstractModule {

    private final String driver;

    public DriverModule() {
        driver = System.getProperty("app.driver", "web");
    }

    public DriverModule(String driver) {
        this.driver = driver;
    }

    @Override
    protected void configure() {
        switch (driver) {
            case "api":
                bind(ApplicationDriver.class).to(ApiApplicationDriver.class);
                break;
            case "web":
                bind(ApplicationDriver.class).to(UiApplicationDriver.class);
                break;
            default:
                throw new ConfigurationError("Invalid value for system property -Dapp.driver. " +
                        "Must be either 'web' or 'api'");
        }
    }
}
