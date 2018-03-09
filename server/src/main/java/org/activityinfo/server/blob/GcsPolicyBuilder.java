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
package org.activityinfo.server.blob;

import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Constructs the policy document that governs what the user may upload
 *
 * @see <a href="https://cloud.google.com/storage/docs/xml-api/post-object#policydocument">Policy Document</a>
 */
public class GcsPolicyBuilder {

    private final JsonArray conditions;
    private final JsonObject document;
    private DateTime expiration;

    public GcsPolicyBuilder() {
        conditions = new JsonArray();
        document = new JsonObject();
        document.add("conditions", conditions);
    }

    public GcsPolicyBuilder expiresAfter(Duration duration) {
        DateTime now = new DateTime();
        expiration = now.plus(duration);
        return this;
    }

    /**
     * Defines a condition that the key of the object to be uploaded is equal to {@code name}
     */
    public GcsPolicyBuilder keyMustEqual(String name) {
        return addExactCondition("key", name);
    }

    public GcsPolicyBuilder bucketNameMustEqual(String name) {
        return addExactCondition("bucket", name);
    }

    public GcsPolicyBuilder contentTypeMustStartWith(String contentTypePrefix) {
        return addStartsWithCondition("Content-Type", contentTypePrefix);
    }

    public GcsPolicyBuilder successActionStatusMustBe(String successActionStatus) {
        return addExactCondition("success_action_status", successActionStatus);
    }

    public GcsPolicyBuilder contentDisposition(String contentDispositionValue) {
        return addExactCondition("content-disposition", contentDispositionValue);
    }

    public GcsPolicyBuilder xGoogMeta(String xGoogMeta, String value) {
        return addExactCondition(xGoogMeta, value);
    }

    public GcsPolicyBuilder contentLengthMustBeBetween(long min, long max) {
        // ["content-length-range", <min_range>, <max_range>].
        JsonArray condition = new JsonArray();
        condition.add(new JsonPrimitive("content-length-range"));
        condition.add(new JsonPrimitive(min));
        condition.add(new JsonPrimitive(max));
        conditions.add(condition);
        return this;
    }

    private GcsPolicyBuilder addExactCondition(String field, String name) {
        JsonObject condition = new JsonObject();
        condition.addProperty(field, name);
        conditions.add(condition);
        return this;
    }

    private GcsPolicyBuilder addStartsWithCondition(String field, String prefix) {
        JsonArray condition = new JsonArray();
        condition.add(new JsonPrimitive("starts-with"));
        condition.add(new JsonPrimitive(field));
        condition.add(new JsonPrimitive(prefix));
        conditions.add(condition);
        return this;
    }

    public byte[] toJsonBytes() {
        return toJson().getBytes(Charsets.UTF_8);
    }

    public String toJson() {
        if (expiration == null) {
            throw new IllegalStateException("Expiration date must be set");
        }

        document.addProperty("expiration", expiration.toString(ISODateTimeFormat.dateTime().withZoneUTC()));

        return document.toString();
    }
}
