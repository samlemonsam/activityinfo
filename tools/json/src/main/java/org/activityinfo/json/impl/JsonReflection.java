package org.activityinfo.json.impl;

import jsinterop.annotations.JsType;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Provides server-side serialization of @JsType annotated types for compatibility with the browser
 */
public class JsonReflection {

    public static JsonValue toJson(Object o) {
        if(o == null) {
            return null;
        }
        if(o instanceof JsonValue) {
            return (JsonValue) o;
        }

        // Wrap basic types
        if(o instanceof Number) {
            return Json.create(((Number) o).doubleValue());
        }
        if(o instanceof String) {
            return Json.create((String) o);
        }
        if(o instanceof Boolean) {
            return Json.create(o == Boolean.TRUE);
        }

        // Arrays...
        if(o.getClass().isArray()) {
            return toArray(o);
        }

        // @JsType
        if(o.getClass().getAnnotation(JsType.class) != null) {
            return toObject(o);
        }

        throw new UnsupportedOperationException("Cannot serialize object of type " + o.getClass() +
                ": the type must either be a subclass of JsonValue or annotated with @JsType");
    }


    private static JsonObject toObject(Object o) {
        JsonObject object = Json.createObject();

        for (Field field : o.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                object.put(field.getName(), toJson(field.get(o)));
            } catch (Exception e) {
                throw new RuntimeException("Could not serialize field " + field.getName() + " on object" +
                    " of type " + o.getClass().getName(), e);
            }
        }

        return object;
    }

    private static JsonValue toArray(Object o) {

        JsonArray array = Json.createArray();

        int length = Array.getLength(o);
        for (int i = 0; i < length; i++) {
            array.add(toJson(Array.get(o, i)));
        }

        return array;
    }

}
