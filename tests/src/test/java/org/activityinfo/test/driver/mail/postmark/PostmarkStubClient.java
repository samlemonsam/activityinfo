package org.activityinfo.test.driver.mail.postmark;

import com.google.common.base.Optional;
import org.activityinfo.test.driver.mail.EmailDriver;
import org.activityinfo.test.driver.mail.NotificationEmail;
import org.activityinfo.test.sut.UserAccount;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Long.toHexString;

public class PostmarkStubClient implements EmailDriver {

    public PostmarkStubClient() throws IOException {
        PostmarkStubServer.start();
    }

    @Override
    public UserAccount newAccount() {
        return new UserAccount(toHexString(ThreadLocalRandom.current().nextLong()) + "@example.com", "notasecret");
    }

    @Override
    public Optional<NotificationEmail> lastNotificationFor(UserAccount account) throws IOException {
        for (Message sentMessage : PostmarkStubServer.SENT_MESSAGES) {
            if(sentMessage.getTo().equals(account.getEmail())) {
                return Optional.of(new NotificationEmail(sentMessage.getSubject(), sentMessage.getTextBody()));
            }
        }
        return Optional.absent();
    }
}
