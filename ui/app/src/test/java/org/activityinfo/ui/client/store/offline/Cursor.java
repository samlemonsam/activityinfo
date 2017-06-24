package org.activityinfo.ui.client.store.offline;

import org.activityinfo.json.JsonValue;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;

class Cursor implements IDBCursor {

    private IDBCursorCallback callback;
    private Iterator<Map.Entry<ObjectKey, JsonValue>> iterator;
    private Map.Entry<ObjectKey, JsonValue> current;

    private ArrayDeque<Runnable> queue = new ArrayDeque<>();

    public Cursor(Iterator<Map.Entry<ObjectKey, JsonValue>> iterator, IDBCursorCallback callback) {
        this.iterator = iterator;
        this.callback = callback;
    }

    @Override
    public void continue_() {
        if(iterator.hasNext()) {
            current = iterator.next();
            queue.push(() -> callback.onNext(Cursor.this));
        } else {
            queue.push(() -> callback.onDone());
        }
    }

    @Override
    public String getKeyString() {
        return current.getKey().toKeyString();
    }

    @Override
    public String[] getKeyArray() {
        return current.getKey().toKeyArray();
    }

    @Override
    public JsonValue getValue() {
        return current.getValue();
    }


    public void run() {
        continue_();
        while(!queue.isEmpty()) {
            queue.poll().run();
        }
    }
}
