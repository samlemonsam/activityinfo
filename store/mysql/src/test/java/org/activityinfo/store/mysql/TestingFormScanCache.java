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
package org.activityinfo.store.mysql;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import org.activityinfo.store.query.shared.FormScanCache;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public class TestingFormScanCache implements FormScanCache {

    private Map<String, byte[]> cache = new HashMap<>();


    @Override
    public Future<Integer> enqueuePut(Map<String, Object> toPut) {
        for (Map.Entry<String, Object> entry : toPut.entrySet()) {
            cache.put(entry.getKey(), serialize(entry.getValue()));
        }
        return Futures.immediateFuture(toPut.size());
    }


    @Override
    public Map<String, Object> getAll(Set<String> keys) {
        Map<String, Object> fetched = Maps.newHashMap();
        for (String key : keys) {
            if(cache.containsKey(key)) {
                fetched.put(key, deserialize(key, cache.get(key)));
            }
        }
        return fetched;
    }

    private byte[] serialize(Object value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new AssertionError("Could not serialize object of type " + value.getClass().getName(), e);
        }
    }


    private Object deserialize(String key, byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            throw new AssertionError("Failed to deserialize key " + key);
        }
    }

    @Override
    public void waitForCachingToFinish(List<Future<Integer>> pendingCachePuts) {
        // NOOP
    }

}
