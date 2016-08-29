package org.activityinfo.model.form;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
    
    public static String fromEnumValue(JsonElement element) {
        if(element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if(object.has("value")) {
                return object.get("value").getAsString().toUpperCase();
            }
            if(object.has("id")) {
                return object.get("id").getAsString().toUpperCase();
            }
        } else if(element.isJsonPrimitive()) {
            return element.getAsString().toUpperCase();
        }
        throw new IllegalArgumentException("element: " + element);
    }
    
}
