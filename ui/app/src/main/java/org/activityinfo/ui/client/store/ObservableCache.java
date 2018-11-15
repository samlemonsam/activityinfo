package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import org.activityinfo.observable.Observable;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A cache for {@link Observable}s.
 *
 * <p>Items remain in the cache as long as they are being observed. Unobserved items are purged on a timer.</p>
 *
 * @param <K>
 * @param <V>
 */
public class ObservableCache<K, V> {

    private static final int CLEANUP_DELAY_MS = (int) TimeUnit.SECONDS.toMillis(90);

    private static class CacheEntry<V>  {
        private Observable<V> value;
        private Date lastConnected = new Date();

        public CacheEntry(Observable<V> value) {
            this.value = value;
        }
    }

    private Map<K, CacheEntry<V>> map = new HashMap<>();

    public ObservableCache() {
        if(GWT.isClient()) {
            Scheduler.get().scheduleFixedPeriod(this::sweepCache, CLEANUP_DELAY_MS);
        }
    }

    private boolean sweepCache() {
        Iterator<Map.Entry<K, CacheEntry<V>>> it = map.entrySet().iterator();
        while(it.hasNext()) {
            CacheEntry<V> entry = it.next().getValue();
            if(!entry.value.isConnected()) {
                it.remove();
            }
        }
        // Schedule again
        return true;
    }

    public void putIfAbsent(K key, Observable<V> value) {
        map.putIfAbsent(key, new CacheEntry<>(value));
    }

    public boolean contains(K key) {
        return map.containsKey(key);
    }

    public Observable<V> get(K key, Supplier<Observable<V>> loader) {
        CacheEntry<V> entry = map.get(key);
        if(entry == null) {
            entry = new CacheEntry<>(loader.get());
            map.put(key, entry);
        }
        return entry.value;
    }

    public Observable<V> getIfPresent(K key) {
        CacheEntry<V> entry = map.get(key);
        if(entry != null) {
            return entry.value;
        }
        return null;
    }
}
