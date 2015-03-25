package org.activityinfo.test.capacity.action;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.activityinfo.test.capacity.Metrics;
import org.activityinfo.test.driver.ApiApplicationDriver;

import java.util.List;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;


public class SyncOfflineWithApi implements UserAction {
    
    private static final Logger LOGGER = Logger.getLogger(SyncOfflineWithApi.class.getName());
    
    public static final UserAction INSTANCE = new SyncOfflineWithApi();

    public static final Histogram REGION_COUNT_METRIC = Metrics.REGISTRY.histogram(name("sync", "region", "count"));
    public static final Histogram TOTAL_SIZE_METRIC = Metrics.REGISTRY.histogram(name("sync", "size"));


    private final LoadingCache<String, RegionMetrics> regionMetrics;
    
    private static class RegionMetrics {
        private Histogram size;
        private Timer latency;
        private Meter succeeded;
        private Meter failed;
        
        private RegionMetrics(String id) {
            size = Metrics.REGISTRY.histogram(name("sync", "size", id));
            latency = Metrics.REGISTRY.timer(name("sync", "latency", id));
            succeeded = Metrics.REGISTRY.meter(name("sync", "succeeded", id));
            failed = Metrics.REGISTRY.meter(name("sync", "failed", id));
        }
    }
    

    private SyncOfflineWithApi() {
        
        regionMetrics = CacheBuilder.newBuilder().concurrencyLevel(150).build(new CacheLoader<String, RegionMetrics>() {
            @Override
            public RegionMetrics load(String key) throws Exception {
                int slash = key.indexOf('/');
                if(slash == -1) {
                    return new RegionMetrics(key);
                } else {
                    return new RegionMetrics(key.substring(0, slash));
                }
            }
        });
    }

    @Override
    public void execute(ApiApplicationDriver driver) throws Exception {
        List<String> syncRegions;
        try {
            syncRegions = driver.getSyncRegions();
            
        } catch (Exception e) {
            LOGGER.fine(String.format("%s: Sync failed to retrieve regions: %s",
                    driver.getCurrentUser(), e.getMessage()));
            throw e;
        }

        LOGGER.fine(String.format("%s: %d sync regions", driver.getCurrentUser(), syncRegions.size()));
        REGION_COUNT_METRIC.update(syncRegions.size());
        
        long totalBytesTransferred = 0;
        for (String id : syncRegions) {
            RegionMetrics metrics = regionMetrics.get(id);
            Timer.Context regionTimer = metrics.latency.time();
            try {
                long bytesTransferred = driver.fetchSyncRegion(id);
                metrics.size.update(bytesTransferred);
                metrics.succeeded.mark();
                totalBytesTransferred += bytesTransferred;
                regionTimer.stop();
                
            } catch (Exception e) {
                metrics.failed.mark();
                LOGGER.fine(String.format("%s: Sync failed on region %s: %s",
                        driver.getCurrentUser(), id, e.getMessage()));
                throw e;
            }
        }
        TOTAL_SIZE_METRIC.update(totalBytesTransferred);

        LOGGER.fine(String.format("%s: Sync completed [%.0f kb]",
                driver.getCurrentUser(), totalBytesTransferred/1024d));
      
    }

    @Override
    public String toString() {
        return "Sync";
    }
}
