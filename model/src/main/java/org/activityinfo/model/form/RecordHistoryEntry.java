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
package org.activityinfo.model.form;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonMappingException;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class RecordHistoryEntry {

    private String formId;
    private String recordId;
    private int time;
    private String subFieldId;
    private String subFieldLabel;
    private String subRecordKey;
    private String changeType;
    private String userName;
    private String userEmail;

    private FieldValueChange[] values;

    public RecordHistoryEntry() {
    }

    @JsOverlay
    public String getFormId() {
        return formId;
    }

    @JsOverlay
    public String getRecordId() {
        return recordId;
    }

    @JsOverlay
    public int getTime() {
        return time;
    }

    @JsOverlay
    public String getSubFieldId() {
        return subFieldId;
    }

    @JsOverlay
    public String getSubFieldLabel() {
        return subFieldLabel;
    }

    @JsOverlay
    public String getSubRecordKey() {
        return subRecordKey;
    }

    @JsOverlay
    public String getChangeType() {
        return changeType;
    }

    @JsOverlay
    public String getUserName() {
        return userName;
    }

    @JsOverlay
    public String getUserEmail() {
        return userEmail;
    }

    @JsOverlay
    public List<FieldValueChange> getValues() {
        return Arrays.asList(values);
    }

    public static class Builder {

        private RecordHistoryEntry entry = new RecordHistoryEntry();
        private List<FieldValueChange> changes = new ArrayList<>();


        /**
         * Sets the formId.
         *
         * @param formId id of the form
         */
        public Builder setFormId(String formId) {
            entry.formId = formId;
            return this;
        }

        /**
         * Sets the recordId.
         *
         * @param recordId id of the record
         */
        public Builder setRecordId(String recordId) {
            entry.recordId = recordId;
            return this;
        }

        /**
         * Sets the time.
         *
         * @param time the time, in seconds since 1970-01-01, that the change was made
         */
        public Builder setTime(int time) {
            entry.time = time;
            return this;
        }

        /**
         * Sets the subFieldId.
         *
         * @param subFieldId for sub records, the subForm field to which this sub record belongs
         */
        public Builder setSubFieldId(String subFieldId) {
            entry.subFieldId = subFieldId;
            return this;
        }

        /**
         * Sets the subFieldLabel.
         *
         * @param subFieldLabel for sub records, the label of the subForm field to which this sub record belongs
         */
        public Builder setSubFieldLabel(String subFieldLabel) {
            entry.subFieldLabel = subFieldLabel;
            return this;
        }

        /**
         * Sets the subRecordKey.
         *
         * @param subRecordKey for keyed sub forms, such as monthly, weekly, or daily subForms, this is a human readable label describing the key, for example '2016-06'
         */
        public Builder setSubRecordKey(String subRecordKey) {
            entry.subRecordKey = subRecordKey;
            return this;
        }

        /**
         * Sets the changeType.
         *
         */
        public Builder setChangeType(String changeType) {
            entry.changeType = changeType;
            return this;
        }

        /**
         * Sets the userName.
         *
         * @param userName the name of the user who made the change
         */
        public Builder setUserName(String userName) {
            entry.userName = userName;
            return this;
        }

        /**
         * Sets the userEmail.
         *
         * @param userEmail the email address of the user who made the change
         */
        public Builder setUserEmail(String userEmail) {
            entry.userEmail = userEmail;
            return this;
        }

        /**
         * Adds a value.
         *
         */
        public Builder addValue(FieldValueChange value) {
            changes.add(value);
            return this;
        }

        public RecordHistoryEntry build() {
            entry.values = changes.toArray(new FieldValueChange[changes.size()]);
            return entry;
        }
    }
}
