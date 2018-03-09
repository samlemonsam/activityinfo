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
