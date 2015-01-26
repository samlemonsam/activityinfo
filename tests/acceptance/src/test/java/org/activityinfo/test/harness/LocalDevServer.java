package org.activityinfo.test.harness;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.PredefinedAccounts;
import org.activityinfo.test.sut.SystemUnderTest;

/**
 * Configures the acceptance tests to run against
 * a locally running AppEngine development server using PhantomJS
 */
public class LocalDevServer extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    public Accounts provideAccounts() {
        return PredefinedAccounts.fromResource(LocalDevServer.class, "devserver-credentials.properties");
    }

    @Provides
    public SystemUnderTest provideSystemUnderTest() {
        return new SystemUnderTest("http://localhost:8080");
    }
}
