package org.activityinfo.server.digest;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Preconditions;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.util.date.DateCalc;

import java.util.Date;

/**
 * @author yuriyz on 10/28/2014.
 */
public class UserDigest {

    private final User user;
    private final Date date;
    private final int days;
    private final long from;
    private final String userSecureToken;

    public UserDigest(User user, Date date, int days, String userSecureToken) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(date);
        Preconditions.checkNotNull(userSecureToken);

        this.user = user;
        this.date = date;
        this.days = days;
        this.from = DateCalc.daysAgo(date, days).getTime();
        this.userSecureToken = userSecureToken;
    }

    public String getUnsubscribeLink() {
        return "https://www.activityinfo.org/subscribe?subscribe=false&token=" + userSecureToken;
    }

    public User getUser() {
        return user;
    }

    public Date getDate() {
        return date;
    }

    public int getDays() {
        return days;
    }

    public long getFrom() {
        return from;
    }

    public Date getFromDate() {
        return new Date(from);
    }
}
