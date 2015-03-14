package org.activityinfo.test.sut;

import com.google.inject.AbstractModule;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.config.ConfigProperty;


public class SystemUnderTest extends AbstractModule {

    public static final ConfigProperty TEST_URL = new ConfigProperty("test.url", "Root URL to Test");
    
    private static final String LOCAL_URL = "http://localhost:8080/";

    @Override
    protected void configure() {
        String url = TEST_URL.getOr(LOCAL_URL);
        
        bind(Server.class).toInstance(new Server(url));
        bind(Accounts.class).to(DevServerAccounts.class).in(ScenarioScoped.class);
    }
}
