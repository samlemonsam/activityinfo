package org.activityinfo.json;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JsonArrays {

    public static <T> List<T> fromJsonArray(Class<T> componentClass, JsonValue array) throws JsonMappingException {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(Json.fromJson(componentClass, array.get(i)));
        }
        return list;
    }

    public static <T> JsonValue toJsonArray(Iterable<T> elements, Function<T, JsonValue> serializer) {
        JsonValue array = Json.createArray();
        for (T element : elements) {
            array.add(serializer.apply(element));
        }
        return array;
    }

    public static JsonValue toJsonArrayFromEnums(Iterable<? extends Enum<?>> elements) {
        JsonValue array = Json.createArray();
        for (Enum<?> element : elements) {
            array.add(Json.create(element.name()));
        }
        return array;
    }

    public static JsonValue toJsonArray(Iterable<? extends JsonSerializable> objects) {
        JsonValue array = Json.createArray();
        for (JsonSerializable object : objects) {
            array.add(object.toJson());
        }
        return array;
    }
}
