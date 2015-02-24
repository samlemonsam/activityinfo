package org.activityinfo.test.sut;

import com.google.inject.AbstractModule;
import org.activityinfo.test.config.ConfigProperty;

import static org.activityinfo.test.sut.PredefinedAccounts.fromResource;


public class SystemUnderTest extends AbstractModule {

    public static final ConfigProperty TEST_URL = new ConfigProperty("test.url", "Root URL to Test");
    
    private static final String PRODUCTION_URL = "https://www.activityinfo.org";

    @Override
    protected void configure() {
        String url = TEST_URL.getOr(PRODUCTION_URL);
        
        bind(Server.class).toInstance(new Server(url));
        
        if(url.startsWith("http://localhost:")) {
            bind(Accounts.class).to(DevServerAccounts.class);
        } else {
            bind(Accounts.class).toInstance(fromResource(getClass(), "devserver-credentials.properties"));
        }
    }
}
