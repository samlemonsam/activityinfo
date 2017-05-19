package org.activityinfo.store.query.impl;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Futures;

import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppEngineFormScanCache implements FormScanCache {

    private static final Logger LOGGER = Logger.getLogger(AppEngineFormScanCache.class.getName());

    private AsyncMemcacheService memcacheService = MemcacheServiceFactory.getAsyncMemcacheService();
    private final List<Future<Set<String>>> pendingCaching = new ArrayList<>();


    @Override
    public Map<String, Object> getAll(Set<String> keys) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Map<String, Object> cached = memcacheService.getAll(keys).get();

            LOGGER.info("Retrieved " + cached.size() + "/" + keys.size() + " requested keys from memcache in " +
                    stopwatch);

            return memcacheService.getAll(keys).get();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception while retrieving columns from cache", e);

            return Collections.emptyMap();
        }
    }

    @Override
    public Future<Integer> enqueuePut(Map<String, Object> toPut) {
        Future<Set<String>> result = memcacheService.putAll(toPut, Expiration.byDeltaSeconds(3600),
                MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);

        return Futures.lazyTransform(result, new Function<Set<String>, Integer>() {
            @Override
            public Integer apply(Set<String> strings) {
                return strings.size();
            }
        });
    }

}
