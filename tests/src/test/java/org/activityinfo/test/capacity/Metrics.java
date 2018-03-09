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
package org.activityinfo.test.capacity;

import com.codahale.metrics.*;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Metrics {

    public static final MetricRegistry REGISTRY = new MetricRegistry();

    private static CsvReporter CSV_REPORTER;
    
    private static final Logger LOGGER = Logger.getLogger(Metrics.class.getName());

    private static final ConcurrentHashMap<Metric, Long> LAST_UPDATE = new ConcurrentHashMap<>();

    public static void start() throws Exception {
        File metricsDir = new File("metrics");
        if(!metricsDir.exists()) {
            metricsDir.mkdirs();
        }
        CSV_REPORTER = CsvReporter.forRegistry(REGISTRY)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(metricsDir);

        CSV_REPORTER.start(1, TimeUnit.SECONDS);
        
    }

    public static void stop() {
        CSV_REPORTER.stop();
    }

    public static void log(String label, Meter meter) {
        if(timeForUpdate(meter)) {
            LOGGER.log(Level.INFO, String.format("%20s: %.0f", label, meter.getOneMinuteRate()));
        }
    }

    public static void log(String label, Timer timer) {
        LOGGER.log(Level.INFO, String.format("%20s: %0f", label, timer.getMeanRate()));
    }
    
    private static boolean timeForUpdate(Metric metric) {
        Long lastUpdate = LAST_UPDATE.get(metric);
        if(lastUpdate == null) {
            LAST_UPDATE.put(metric, System.currentTimeMillis());
        } else {
            if((System.currentTimeMillis()-lastUpdate) > 10000) {
                LAST_UPDATE.put(metric, System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }
}
