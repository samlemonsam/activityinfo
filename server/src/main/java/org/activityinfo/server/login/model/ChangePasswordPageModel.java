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
package org.activityinfo.server.login.model;

import org.activityinfo.server.database.hibernate.entity.User;

public class ChangePasswordPageModel extends PageModel {

    private User user;
    private boolean passwordLengthInvalid = false; // if less than 6 chars
    private boolean passwordsNotMatched = false;

    public ChangePasswordPageModel(User user) {
        super();
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean isPasswordLengthInvalid() {
        return passwordLengthInvalid;
    }

    public ChangePasswordPageModel setPasswordLengthInvalid(boolean passwordLengthInvalid) {
        this.passwordLengthInvalid = passwordLengthInvalid;
        return this;
    }

    public boolean isPasswordsNotMatched() {
        return passwordsNotMatched;
    }

    public ChangePasswordPageModel setPasswordsNotMatched(boolean passwordsNotMatched) {
        this.passwordsNotMatched = passwordsNotMatched;
        return this;
    }
}
