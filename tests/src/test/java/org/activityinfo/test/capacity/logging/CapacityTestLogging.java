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
package org.activityinfo.test.capacity.logging;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import org.activityinfo.test.capacity.Metrics;
import org.activityinfo.test.capacity.action.ActionExecution;
import org.activityinfo.test.driver.ApplicationDriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Sets up logging for the test run
 */
public class CapacityTestLogging {


    private static ScheduledExecutorService SCHEDULER;

    public static void setup() throws IOException {
        // Divert System.err because webDriver just dumps everything without synchronization
        // System.setErr(new PrintStream(new NullOutputStream()));


        try(InputStream in = Resources.getResource(CapacityTestLogging.class, "logging.properties").openStream()) {
            LogManager.getLogManager().readConfiguration(in);
        }

        SCHEDULER = Executors.newSingleThreadScheduledExecutor();
        SCHEDULER.scheduleWithFixedDelay(new MetricLogger(), 10, 10, TimeUnit.SECONDS);
    }

    public static void stop() throws InterruptedException {
        SCHEDULER.shutdown();
        SCHEDULER.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static class MetricLogger implements Runnable {
        
        private Logger logger = Logger.getLogger(Metrics.class.getName());


        public MetricLogger() {
        }

        @Override
        public void run() {
            logger.log(Level.INFO, Joiner.on("   ").join(concurrentUsers(), throughput(), failure()));
                
        }
        
        private String concurrentUsers() {
            return String.format("users[%3d]", ActionExecution.CONCURRENT_USERS.getCount());
        }
        
        private String throughput() {
            return String.format("throughput[%4.1f qps]",  ApplicationDriver.COMMAND_RATE.getOneMinuteRate());
        }
        
        private String failure() {
            long count = ApplicationDriver.ERROR_RATE.getTotalErrorCount();
            if(count == 0) {
                return "";
            } else {
                return String.format("errors[%4d]", count);
            }
        }

    }
}
