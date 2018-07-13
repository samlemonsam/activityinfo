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

import org.activityinfo.json.JsonValue;

public class BounceReport {

    private String recordType;
    private String id;
    private String type;
    private int typeCode;
    private String name;
    private String tag;
    private String messageId;
    private String description;
    private String details;
    private String email;
    private String bouncedAt;
    private boolean dumpAvailable;
    private boolean inactive;
    private boolean canActivate;
    private String subject;

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBouncedAt() {
        return bouncedAt;
    }

    public void setBouncedAt(String bouncedAt) {
        this.bouncedAt = bouncedAt;
    }

    public boolean isDumpAvailable() {
        return dumpAvailable;
    }

    public void setDumpAvailable(boolean dumpAvailable) {
        this.dumpAvailable = dumpAvailable;
    }

    public boolean isInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    public boolean isCanActivate() {
        return canActivate;
    }

    public void setCanActivate(boolean canActivate) {
        this.canActivate = canActivate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public static BounceReport fromJson(JsonValue object) {
        BounceReport bounceReport = new BounceReport();

        if (!object.hasKey("RecordType")) {
            throw new IllegalArgumentException("No RecordType defined");
        }
        if (object.get("RecordType").isJsonNull() || !object.get("RecordType").asString().equals("Bounce")) {
            throw new IllegalArgumentException("RecordType is not Bounce");
        }

        bounceReport.setRecordType(object.get("RecordType").asString());
        bounceReport.setId(object.get("ID").asString());
        bounceReport.setType(object.get("Type").asString());
        bounceReport.setTypeCode(object.get("TypeCode").asInt());
        bounceReport.setName(object.get("Name").asString());
        bounceReport.setTag(object.get("Tag").asString());
        bounceReport.setMessageId(object.get("MessageID").asString());
        bounceReport.setDescription(object.get("Description").asString());
        bounceReport.setDetails(object.get("Details").asString());
        bounceReport.setEmail(object.get("Email").asString());
        bounceReport.setBouncedAt(object.get("BouncedAt").asString());
        bounceReport.setDumpAvailable(object.get("DumpAvailable").asBoolean());
        bounceReport.setInactive(object.get("Inactive").asBoolean());
        bounceReport.setCanActivate(object.get("CanActivate").asBoolean());
        bounceReport.setSubject(object.get("Subject").asString());

        return bounceReport;
    }

}
