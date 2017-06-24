package org.activityinfo.test.driver.mail;

import com.google.common.base.Optional;
import org.activityinfo.test.sut.UserAccount;

import java.io.IOException;

/**
 * Support tests with the ability to read email notifications sent 
 */
public interface EmailDriver {

    UserAccount newAccount();
    
    Optional<NotificationEmail> lastNotificationFor(UserAccount account) throws IOException;

    
}
