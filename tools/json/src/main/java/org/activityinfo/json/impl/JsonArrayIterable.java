package org.activityinfo.json.impl;

import org.activityinfo.json.JsonValue;

import java.util.Iterator;

public class JsonArrayIterable implements Iterable<JsonValue> {

    private final JsonValue array;

    public JsonArrayIterable(JsonValue array) {
        this.array = array;
    }

    @Override
    public Iterator<JsonValue> iterator() {

        final int length = array.length();

        return new Iterator<JsonValue>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public JsonValue next() {
                return array.get(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
