package org.activityinfo.api.client;

import com.google.gson.JsonElement;

/**
 * Support methods for JSON string
 */
public class JsonParsing {
    
    public static String toNullableString(JsonElement value) {
        if(value == null || value.isJsonNull()) {
            return null;
        } else {
            return value.getAsString();
        }
    }
}
