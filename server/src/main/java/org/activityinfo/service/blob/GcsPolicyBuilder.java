package org.activityinfo.service.blob;

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
 * @see <a href="https://developers.google.com/storage/docs/reference-methods?csw=1#policydocument">Policy Document</a>
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
     *
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
        String json = toJson();
        return json.getBytes(Charsets.UTF_8);
    }

    public String toJson() {
        if(expiration == null) {
            throw new IllegalStateException("Expiration date must be set");
        }
        document.addProperty("expiration", expiration.toString(ISODateTimeFormat.dateTime()));

        return document.toString();
    }
}
