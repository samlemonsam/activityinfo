package org.activityinfo.store.mysql;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import org.activityinfo.store.query.shared.FormScanCache;

import java.io.*;
import java.util.HashMap;
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
}
