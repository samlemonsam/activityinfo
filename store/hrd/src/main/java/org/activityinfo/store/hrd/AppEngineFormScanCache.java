package org.activityinfo.store.hrd;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Futures;
import org.activityinfo.store.query.shared.FormScanCache;

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


    /**
     * Wait for caching to finish, if there is time left in this request.
     */
    @Override
    public void waitForCachingToFinish(List<Future<Integer>> pendingCachePuts) {

        Stopwatch stopwatch = Stopwatch.createStarted();

        int columnCount = 0;
        for (Future<Integer> future : pendingCachePuts) {
            if (!future.isDone()) {
                long remainingMillis = ApiProxy.getCurrentEnvironment().getRemainingMillis();
                if (remainingMillis > 100) {
                    try {
                        Integer cachedCount = future.get(remainingMillis - 50, TimeUnit.MILLISECONDS);
                        columnCount += cachedCount;

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
