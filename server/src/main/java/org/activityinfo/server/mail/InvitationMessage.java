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
package org.activityinfo.server.mail;

import org.activityinfo.server.database.hibernate.entity.User;

public class InvitationMessage extends MessageModel {
    private User newUser;
    private User invitingUser;

    public InvitationMessage(User newUser, User invitingUser) {
        this.newUser = newUser;
        this.invitingUser = invitingUser;
    }

    public User getNewUser() {
        return newUser;
    }

    public User getInvitingUser() {
        return invitingUser;
    }

    @Override
    public User getRecipient() {
        return newUser;
    }
}
