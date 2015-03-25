package org.activityinfo.server.util.monitoring;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;
import org.activityinfo.server.util.config.DeploymentConfiguration;
import org.aopalliance.intercept.MethodInterceptor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitoringModule extends ServletModule {

    private static final Logger LOGGER = Logger.getLogger(MonitoringModule.class.getName());
    
    public static final String STATSD_HOST = "statsd.host";
    public static final String STATSD_PORT = "statsd.port";
    public static final String STATSD_PREFIX = "statsd.prefix";

    public MonitoringModule() {
    }

    @Override
    protected void configureServlets() {

        filter("/*").through(MetricsRequestFilter.class);

        MethodInterceptor interceptor = new ProfilingInterceptor();
        requestInjection(interceptor);


        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Count.class), interceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), interceptor);
    }

    @Provides
    @Singleton
    public MetricsReporter provide(DeploymentConfiguration config) {
        try {
            return new UdpReporter(
                    config.getProperty(STATSD_PREFIX, ""), 
                    config.getProperty(STATSD_HOST, "146.148.16.39"), 
                    config.getIntProperty(STATSD_PORT, 8125));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception creating " + UdpReporter.class.getName() + ", falling back to " + 
                    NullReporter.class.getName());
            
            return new NullReporter();
        }
    }
}
