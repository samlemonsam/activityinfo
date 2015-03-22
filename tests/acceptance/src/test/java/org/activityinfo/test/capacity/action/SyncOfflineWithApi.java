package org.activityinfo.test.capacity.action;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import org.activityinfo.test.capacity.Metrics;
import org.activityinfo.test.driver.ApiApplicationDriver;

import java.util.List;
import java.util.logging.Logger;


public class SyncOfflineWithApi implements UserAction {
    
    private static final Logger LOGGER = Logger.getLogger(SyncOfflineWithApi.class.getName());
    
    public static final UserAction INSTANCE = new SyncOfflineWithApi();
    
    public static final Counter CONCURRENT = Metrics.REGISTRY.counter("sync-concurrent");
    public static final com.codahale.metrics.Timer SYNC_TIME = Metrics.REGISTRY.timer("sync-time");
    public static final Histogram SYNC_BYTES = Metrics.REGISTRY.histogram("sync-bytes");
    public static final Meter SYNC_COMPLETED = Metrics.REGISTRY.meter("sync-complete");
    public static final Meter SYNC_FAILED = Metrics.REGISTRY.meter("sync-failed");

    private SyncOfflineWithApi() {
        
    }

    @Override
    public void execute(ApiApplicationDriver driver) throws Exception {
        String currentRegion = "none";
        CONCURRENT.inc();
        try {
            Timer.Context time = SYNC_TIME.time();
            List<String> syncRegions = driver.getSyncRegions();
            LOGGER.fine(String.format("%s: %d sync regions", driver.getCurrentUser(), syncRegions.size()));
            
            long totalBytesTransferred = 0;
            for (String id : syncRegions) {
                currentRegion = id;
                long bytesTransferred = driver.fetchSyncRegion(id);
                totalBytesTransferred += bytesTransferred;
            }
            SYNC_BYTES.update(totalBytesTransferred);
            SYNC_COMPLETED.mark();
            time.stop();

            LOGGER.fine(String.format("%s: Sync completed [%.0f kb]",
                    driver.getCurrentUser(), totalBytesTransferred/1024d));
       
        } catch (Exception e) {
            LOGGER.fine(String.format("%s: Sync failed [%s]: %s", 
                    driver.getCurrentUser(), currentRegion, e.getMessage()));
            SYNC_FAILED.mark();
            
        } finally {

            CONCURRENT.dec();
        }
    }

    @Override
    public String toString() {
        return "Sync";
    }
}
