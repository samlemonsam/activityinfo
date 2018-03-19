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
package org.activityinfo.json;

import org.activityinfo.json.impl.JreJsonFactory;
import org.activityinfo.json.impl.JsonReflection;
import org.activityinfo.json.impl.JsonUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Vends out implementation of JsonFactory.
 */
public class Json {

    public static JsonValue createFromNullable(String string) {
        return instance().createFromNullable(string);
    }

    public static JsonValue create(String string) {
        return instance().create(string);
    }

    public static JsonValue create(boolean bool) {
        return instance().create(bool);
    }

    public static JsonValue createArray() {
        return instance().createArray();
    }

    public static JsonValue createNull() {
        return instance().createNull();
    }

    public static JsonValue create(double number) {
        return instance().create(number);
    }

    public static JsonValue createObject() {
        return instance().createObject();
    }

    public static JsonFactory instance() {
        return new JreJsonFactory();
    }

    public static JsonValue parse(String jsonString) {
        return instance().parse(jsonString);
    }

    public static String stringify(Object value) {
        return JsonUtil.stringify(Json.toJson(value));
    }

    public static String stringify(JsonValue jsonValue) {
        return JsonUtil.stringify(jsonValue);
    }

    public static String stringify(JsonValue jsonValue, int indent) {
        return JsonUtil.stringify(jsonValue, indent);
    }

    public static JsonValue toJson(Object value) {
        if(value instanceof JsonSerializable) {
            return ((JsonSerializable) value).toJson();

        } else if(value instanceof Collection) {
            JsonValue array = Json.createArray();
            for (Object element : ((Collection) value)) {
                array.add(Json.toJson(element));
            }
            return array;

        } else {
            return JsonReflection.toJson(value);
        }
    }

    public static <T> T fromJson(Class<T> clazz, JsonValue object) throws JsonMappingException {
        return JsonReflection.fromJson(clazz, object);
    }

    public static <T> List<T> fromJsonArray(Class<T> componentClass, JsonValue array) throws JsonMappingException {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(Json.fromJson(componentClass, array.get(i)));
        }
        return list;
    }


    public static JsonValue toJsonArray(Iterable<? extends JsonSerializable> objects) {
        JsonValue array = Json.createArray();
        for (JsonSerializable object : objects) {
            array.add(object.toJson());
        }
        return array;
    }
}
