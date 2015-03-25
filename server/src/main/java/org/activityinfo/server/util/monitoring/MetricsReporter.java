package org.activityinfo.server.util.monitoring;


public interface MetricsReporter {


    /**
     * Increments the counter identified by {@code metricId}
     */
    void increment(String metricId, long count);

    /**
     * Updates the timer metric identified by {@code metricId}
     */
    void time(String metricId, long milliseconds);


    /**
     * Add the item id to the set of unique users, etc.
     */
    void set(String metricId, String id);

    void histogram(String metricId, double value);
    
    void flush();
}
