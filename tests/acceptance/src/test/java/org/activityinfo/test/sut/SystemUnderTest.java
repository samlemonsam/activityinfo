package org.activityinfo.test.sut;

import com.google.inject.AbstractModule;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.mail.mailinator.MailinatorClient;
import org.activityinfo.test.driver.mail.postmark.PostmarkStubServer;


public class SystemUnderTest extends AbstractModule {


    @Override
    protected void configure() {
        
        bind(Server.class).toInstance(new Server());
        bind(Accounts.class).to(DevServerAccounts.class).in(ScenarioScoped.class);
    }
}
