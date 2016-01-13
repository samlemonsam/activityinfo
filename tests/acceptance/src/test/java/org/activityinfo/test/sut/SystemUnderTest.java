package org.activityinfo.test.sut;

import com.google.inject.AbstractModule;
import cucumber.runtime.java.guice.ScenarioScoped;


public class SystemUnderTest extends AbstractModule {

    private Server server;

    public SystemUnderTest(String testUrl) {
        this.server = new Server(testUrl);
    }

    public SystemUnderTest() {
        this.server = new Server();
    }

    @Override
    protected void configure() {
        
        bind(Server.class).toInstance(server);
        bind(Accounts.class).to(DevServerAccounts.class).in(ScenarioScoped.class);
        bind(DevServerAccounts.class).in(ScenarioScoped.class);

    }
}
