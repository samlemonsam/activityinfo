package org.activityinfo.server.util.monitoring;


public class NullReporter implements MetricsReporter {

    @Override
    public void increment(String metricId, long count) {  }

    @Override
    public void time(String metricId, long milliseconds) {  }

    @Override
    public void set(String metricId, String id) {
    }

    @Override
    public void histogram(String metricId, double value) {
        
    }

    @Override
    public void flush() {
        
    }
}
