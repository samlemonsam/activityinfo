package org.activityinfo.store.query.impl;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Stopwatch;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    public void enqueuePut(Map<String, Object> toPut) {
        Future<Set<String>> result = memcacheService.putAll(toPut, Expiration.byDeltaSeconds(3600),
                MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);

        pendingCaching.add(result);

    }

    @Override
    public void waitUntilCached() {

        Stopwatch stopwatch = Stopwatch.createStarted();

        int columnCount = 0;
        for (Future<Set<String>> future : pendingCaching) {
            if (!future.isDone()) {
                long remainingMillis = ApiProxy.getCurrentEnvironment().getRemainingMillis();
                if (remainingMillis > 100) {
                    try {
                        Set<String> cachedKeys = future.get(remainingMillis - 50, TimeUnit.MILLISECONDS);
                        columnCount += cachedKeys.size();

                    } catch (InterruptedException | TimeoutException e) {
                        LOGGER.warning("Ran out of time while waiting for caching of results to complete.");
                        return;

                    } catch (ExecutionException e) {
                        LOGGER.log(Level.WARNING, "Exception caching results of query", e);
                    }
                }
            }
        }

        LOGGER.info("Waited " + stopwatch + " for " + columnCount + " columns to finish caching.");
    }
}
