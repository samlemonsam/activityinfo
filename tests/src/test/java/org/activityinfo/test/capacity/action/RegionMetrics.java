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
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.activityinfo.test.capacity.Metrics;

import java.util.concurrent.ExecutionException;

import static com.codahale.metrics.MetricRegistry.name;


class RegionMetrics {

    private static final LoadingCache<String, RegionMetrics> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(150)
            .build(new RegionMetricsFactory());
    
    final Histogram size;
    final Timer latency;
    final Meter succeeded;
    final Meter failed;
    
    RegionMetrics(String id) {
        size = Metrics.REGISTRY.histogram(name("sync", "size", id));
        latency = Metrics.REGISTRY.timer(name("sync", "latency", id));
        succeeded = Metrics.REGISTRY.meter(name("sync", "succeeded", id));
        failed = Metrics.REGISTRY.meter(name("sync", "failed", id));
    }
    
    private static class RegionMetricsFactory extends CacheLoader<String, RegionMetrics> {

        @Override
        public RegionMetrics load(String key) throws Exception {
            int slash = key.indexOf('/');
            if(slash == -1) {
                return new RegionMetrics(key);
            } else {
                return new RegionMetrics(key.substring(0, slash));
            }
        }
    }

    public static RegionMetrics get(String id) throws ExecutionException {
        return CACHE.get(id);
    }
    
}
