package org.activityinfo.test.driver;

import org.activityinfo.test.driver.mail.MailinatorClient;
import org.activityinfo.test.driver.mail.Message;
import org.activityinfo.test.driver.mail.MessageHeader;
import org.activityinfo.test.sut.UserAccount;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * Provides application-logic on top of an Email client like Mailinator
 */
public class EmailDriver {

    public static final String SENDER_EMAIL = "notifications@activityinfo.org";
    private final MailinatorClient client;

    @Inject
    public EmailDriver(MailinatorClient client) {
        this.client = client;
    }
    
    public NotificationEmail lastNotificationFor(UserAccount account, long afterTime) throws IOException {

        List<MessageHeader> messages = client.queryInbox(account);
        for (MessageHeader header : messages) {
            if (header.getFrom().equals(SENDER_EMAIL) && isAfter(afterTime, header)) {
                Message message = client.queryMessage(header);
                return new NotificationEmail(header.getSubject(), message.getPlainText());
            }
        }
        throw new AssertionError("No emails from " + SENDER_EMAIL + " found.");
    }

    private boolean isAfter(long afterTime, MessageHeader header) {
        if(header.getTime() > afterTime) {
            return true;
        } else {
            return (afterTime - header.getTime()) < 500;
        }
    }
}
