package org.activityinfo.model.form;

import org.activityinfo.json.JsonValue;

/**
 * Support methods for JSON string
 */
public class JsonParsing {
    
    public static String toNullableString(JsonValue value) {
        if(value.isJsonNull()) {
            return null;
        } else {
            return value.asString();
        }
    }
    
    public static String fromEnumValue(JsonValue element) {
        if(element.isJsonObject()) {
            JsonValue object = element;
            if(object.hasKey("value")) {
                return object.get("value").asString().toUpperCase();
            }
            if(object.hasKey("id")) {
                return object.get("id").asString().toUpperCase();
            }
        } else if(element.isJsonPrimitive()) {
            return element.asString().toUpperCase();
        }
        throw new IllegalArgumentException("element: " + element);
    }
    
}
