package org.activityinfo.test.driver.mail;

import com.google.inject.AbstractModule;
import org.activityinfo.test.driver.mail.mailinator.MailinatorClient;
import org.activityinfo.test.driver.mail.postmark.PostmarkStubClient;
import org.activityinfo.test.driver.mail.postmark.PostmarkStubServer;

public class EmailModule extends AbstractModule {
    @Override
    protected void configure() {
        if(PostmarkStubServer.POSTMARK_STUB_PORT.isPresent()) {
            bind(EmailDriver.class).to(PostmarkStubClient.class);

        } else {
            bind(EmailDriver.class).to(MailinatorClient.class);
        }
    }
}
