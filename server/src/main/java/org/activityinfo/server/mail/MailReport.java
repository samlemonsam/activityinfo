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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MailReport {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("TypeCode")
    private int typeCode;

    @JsonProperty("Tag")
    private String tag;

    @JsonProperty("MessageID")
    private String messageId;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("BouncedAt")
    private String bounceDate;

    @JsonProperty("Details")
    private String details;

    @JsonProperty("DumpAvailable")
    private boolean dumpAvailable;

    @JsonProperty("CanActivate")
    private boolean canActivate;

    @JsonProperty("Subject")
    private String subject;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public String getTag() {
        return tag;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getEmail() {
        return email;
    }

    public String getBounceDate() {
        return bounceDate;
    }

    public String getDetails() {
        return details;
    }

    public boolean isDumpAvailable() {
        return dumpAvailable;
    }

    public boolean isCanActivate() {
        return canActivate;
    }

    public String getSubject() {
        return subject;
    }
}
