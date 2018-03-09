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
package org.activityinfo.test.driver.mail;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationEmail {
    private final String subject;
    private final String messageBody;

    public NotificationEmail(String subject, String messageBody) {
        this.subject = subject;
        this.messageBody = messageBody;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public URL extractLink() {
        Pattern linkPattern = Pattern.compile("https?://[^\\s]+[^\\.\\s]");   
        List<URL> links = Lists.newArrayList();
        Matcher matcher = linkPattern.matcher(messageBody);
        while(matcher.find()) {
            String link = matcher.group();
            try {
                links.add(new URL(link));
            } catch (MalformedURLException e) {
                throw new AssertionError("Malformed link '" + link + "': " + e.getMessage());
            }
        }
        if(links.size() == 0) {
            throw new AssertionError("Message contained no links:\n" + messageBody);
        } else if(links.size() > 1) {
            throw new AssertionError("Message contained several links:\n" + Joiner.on("\n").join(links));
        } else {
            return links.get(0);
        }
    }
}
