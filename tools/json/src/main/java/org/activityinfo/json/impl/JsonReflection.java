package org.activityinfo.json.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.JsType;
import org.activityinfo.json.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides server-side serialization of @JsType annotated types for compatibility with the browser
 */
@com.google.gwt.core.shared.GwtIncompatible
@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class JsonReflection {

    private static class FieldMapping {
        private final Field field;
        private String name;
        private boolean required = false;
        public FieldMapping(Field field) {
            this.field = field;
            this.name = field.getName();

            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            if(annotation != null && annotation.required()) {
                required = true;
            }
        }

        public void fromJson(Object instance, JsonObject jsonObject) throws JsonMappingException {
            if(jsonObject.hasKey(name)) {
                try {
                    Object fieldValue = JsonReflection.fromJson(field.getType(), jsonObject.get(name));
                    field.set(instance, fieldValue);
                } catch (JsonMappingException e) {
                    throw new JsonMappingException(String.format(
                        "Invalid value for property '%s': %s", name, e.getMessage()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not deserialize field " + field.getName() + " on object" +
                        " of type " + instance.getClass().getName(), e);
                }
            } else if(required) {
                throw new JsonMappingException(String.format("Missing required field '%s'", name));
            }
        }

        public <T> void toJson(T object, JsonObject jsonObject) {
            try {
                jsonObject.put(name, JsonReflection.toJson(field.get(object)));
            } catch (Exception e) {
                throw new RuntimeException("Could not serialize field " + field.getName() + " on object" +
                    " of type " + object.getClass().getName(), e);
            }
        }
    }

    private static class ClassMapping<T> {
        private List<FieldMapping> fields = new ArrayList<>();
        private Class<T> clazz;

        public ClassMapping(Class<T> clazz) {
            this.clazz = clazz;
            for (Field field : clazz.getDeclaredFields()) {
                if(!shouldIgnore(field)) {
                    field.setAccessible(true);
                    fields.add(new FieldMapping(field));
                }
            }
        }

        private boolean shouldIgnore(Field field) {
            // Jacoco adds extra fields that we shouldn't touch...
            if(field.getName().startsWith("$")) {
                return true;
            }
            return false;
        }

        public T fromJson(JsonObject object) throws JsonMappingException {
            T o = null;
            try {
                o = clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not create a new instance of @JsType " + clazz.getName(), e);
            }

            for (FieldMapping field : fields) {
                field.fromJson(o, object);
            }

            return o;
        }

        public JsonObject toJson(T object) {
            JsonObject jsonObject = Json.createObject();

            for (FieldMapping field : fields) {
                field.toJson(object, jsonObject);
            }

            return jsonObject;
        }
    }

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
            return Json.create(((Boolean) o));
        }

        if(o instanceof Date) {
            return Json.create(o.toString());
        }

        // Arrays...
        if(o.getClass().isArray()) {
            return toArray(o);
        }

        // @JsType
        if(isJsType(o.getClass())) {
            return toJsonObject(o);
        }

        throw new UnsupportedOperationException("Cannot serialize object of type " + o.getClass() +
            ": the type must either be a subclass of JsonValue or annotated with @JsType");
    }

    private static boolean isJsType(Class<?> aClass) {
        return aClass.getAnnotation(JsType.class) != null;
    }

    public static <T> T fromJson(Class<T> clazz, JsonValue value) throws JsonMappingException {

        if(value == null || value.isJsonNull()) {
            return null;
        }

        if(clazz.equals(JsonValue.class)) {
            return (T) value;
        }

        if(clazz.equals(JsonObject.class)) {
            assertType(JsonType.OBJECT, value);
            return (T) value.getAsJsonObject();
        }

        if(clazz.equals(JsonArray.class)) {
            assertType(JsonType.ARRAY, value);
            return (T) value.getAsJsonArray();
        }

        if(clazz.equals(String.class)) {
            assertPrimitive(JsonType.STRING, value);
            return (T)value.asString();
        }

        if(clazz.equals(boolean.class)) {
            assertPrimitive(JsonType.BOOLEAN, value);
            return (T) Boolean.valueOf(value.asBoolean());
        }

        if(clazz.equals(int.class)) {
            assertPrimitive(JsonType.NUMBER, value);
            return (T) Integer.valueOf(value.asInt());
        }

        if(clazz.equals(double.class)) {
            assertPrimitive(JsonType.NUMBER, value);
            return (T) Double.valueOf(value.asNumber());
        }

        if(clazz.isArray()) {
            assertType(JsonType.ARRAY, value);
            return (T)fromArray(clazz, value.getAsJsonArray());
        }

        if(isJsType(clazz)) {
            assertType(JsonType.OBJECT, value);
            return new ClassMapping<>(clazz).fromJson(value.getAsJsonObject());
        }


        throw new UnsupportedOperationException("Type: " + clazz.getName());

    }

    private static void assertPrimitive(JsonType expectedType, JsonValue value) throws JsonMappingException {
        if(value.isJsonObject() || value.isJsonArray()) {
            throw new JsonMappingException(String.format("Expected value of type '%s', but found '%s'",
                expectedType.name(), value.getType().name()));
        }
    }

    private static void assertType(JsonType expectedType, JsonValue value) throws JsonMappingException {
        if(value.getType() != expectedType) {
            throw new JsonMappingException(String.format("Expected value of type '%s', but found '%s'",
                expectedType.name(), value.getType().name()));
        }
    }

    private static Object fromArray(Class arrayType, JsonArray jsonArray) throws JsonMappingException {
        Class componentType = arrayType.getComponentType();
        Object array = Array.newInstance(componentType, jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            Array.set(array, i, Json.fromJson(componentType, jsonArray.get(i)));
        }

        return array;
    }

    private static JsonObject toJsonObject(Object o) {
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
