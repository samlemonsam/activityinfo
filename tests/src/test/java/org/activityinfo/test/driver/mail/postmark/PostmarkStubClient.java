/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
