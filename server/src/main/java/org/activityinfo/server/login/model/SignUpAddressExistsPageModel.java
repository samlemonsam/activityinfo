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

import com.sun.jersey.api.view.Viewable;




public class SignUpAddressExistsPageModel extends PageModel {
    private String email = "";
    private boolean loginError;
    private boolean emailSent;
    private boolean emailError;


    public SignUpAddressExistsPageModel() {
    }

    public SignUpAddressExistsPageModel(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public boolean isLoginError() {
        return loginError;
    }

    public Viewable asLoginError() {
        this.loginError = true;
        return this.asViewable();
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public Viewable asEmailSent() {
        this.emailSent = true;
        return this.asViewable();
    }

    public boolean isEmailError() {
        return emailError;
    }

    public Viewable asEmailError() {
        this.emailError = true;
        return this.asViewable();
    }
}
