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
package org.activityinfo.test.capacity.action;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import org.activityinfo.test.capacity.Metrics;
import org.activityinfo.test.driver.ApiApplicationDriver;
import org.activityinfo.test.driver.SyncRegion;
import org.activityinfo.test.driver.SyncUpdate;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;


public class SynchronizeAction implements UserAction {
    
    private static final Logger LOGGER = Logger.getLogger(SynchronizeAction.class.getName());
    
    public static final Histogram REGION_COUNT_METRIC = Metrics.REGISTRY.histogram(name("sync", "region", "count"));
    public static final Histogram TOTAL_SIZE_METRIC = Metrics.REGISTRY.histogram(name("sync", "size"));
   
    private final ConcurrentMap<String, String> localVersions;

    public SynchronizeAction() {
        this.localVersions = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(ApiApplicationDriver driver) throws Exception {
        List<SyncRegion> syncRegions;
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
        for (SyncRegion region : syncRegions) {
            String id = region.getId();
            while(!region.getVersion().equals(localVersions.get(id))) {
                RegionMetrics metrics = RegionMetrics.get(id);
                Timer.Context regionTimer = metrics.latency.time();
                try {
                    SyncUpdate update = driver.fetchSyncRegion(region.getId(), localVersions.get(id));
                    localVersions.put(id, update.getVersion());

                    metrics.size.update(update.getByteCount());
                    metrics.succeeded.mark();
                    totalBytesTransferred += update.getByteCount();
                    regionTimer.stop();
                    
                    if(update.isComplete()) {
                        break;
                    }
                    
                } catch (Exception e) {
                    metrics.failed.mark();
                    LOGGER.fine(String.format("%s: Sync failed on region %s: %s",
                            driver.getCurrentUser(), id, e.getMessage()));
                    throw e;
                }
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
