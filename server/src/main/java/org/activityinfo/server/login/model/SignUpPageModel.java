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

import com.google.appengine.labs.repackaged.com.google.common.base.Strings;

public class SignUpPageModel extends PageModel {
    // used for form population after a continuable message
    private String email = "";
    private String name = "";
    private String organization = "";
    private String jobtitle = "";
    private String locale = "";

    // messages
    private boolean formError;
    private boolean genericError;
    private boolean confirmationEmailSent;

    public SignUpPageModel() {
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getOrganization() {
        return organization;
    }

    public String getJobtitle() {
        return jobtitle;
    }

    public String getLocale() {
        return locale;
    }

    public SignUpPageModel set(String email, String name, String organization, String jobtitle, String locale) {
        this.email = Strings.nullToEmpty(email);
        this.name = Strings.nullToEmpty(name);
        this.organization = Strings.nullToEmpty(organization);
        this.jobtitle = Strings.nullToEmpty(jobtitle);
        this.locale = Strings.nullToEmpty(locale);
        return this;
    }

    public boolean isFormError() {
        return formError;
    }

    public boolean isGenericError() {
        return genericError;
    }

    public boolean isConfirmationEmailSent() {
        return confirmationEmailSent;
    }

    public static SignUpPageModel formErrorModel() {
        SignUpPageModel model = new SignUpPageModel();
        model.formError = true;
        return model;
    }

    public static SignUpPageModel genericErrorModel() {
        SignUpPageModel model = new SignUpPageModel();
        model.genericError = true;
        return model;
    }

    public static SignUpPageModel confirmationEmailSentModel() {
        SignUpPageModel model = new SignUpPageModel();
        model.confirmationEmailSent = true;
        return model;
    }
}
