package org.activityinfo.indexedb;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class Cursor<T> implements IDBCursor<T> {

    private static final Logger LOGGER = Logger.getLogger(Cursor.class.getName());

    private final ObjectStoreStub.Transaction tx;
    private IDBCursorCallback callback;
    private Iterator<Map.Entry<ObjectKey, T>> iterator;
    private Map.Entry<ObjectKey, T> current;

    private ArrayDeque<Runnable> queue = new ArrayDeque<>();

    public Cursor(ObjectStoreStub<T>.Transaction tx, Iterator<Map.Entry<ObjectKey, T>> iterator, IDBCursorCallback callback) {
        this.tx = tx;
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
    public int getKeyNumber() {
        return current.getKey().toKeyNumber();
    }

    @Override
    public T getValue() {
        return current.getValue();
    }

    @Override
    public void update(T value) {
        tx.put(current.getKey(), value);
    }


    public void run() {
        try {
            continue_();
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        } catch (Throwable caught) {
            LOGGER.log(Level.SEVERE, "Uncaught exception while running IndexedDB cursor: " + caught.getMessage(), caught);
            throw new RuntimeException(caught);
        }
    }
}
