package org.activityinfo.server.util.monitoring;


import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Metrics {
    
    private final MetricsReporter reporter;

    @Inject
    public Metrics(MetricsReporter reporter) {
        this.reporter = reporter;
    }

    public static String name(String... path) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<path.length;++i) {
            if(sb.length() > 0) {
                sb.append('.');
            }
            sb.append(path[i]);
        }
        return sb.toString();
    }
    
    public Profiler profile(String... path) {
        return new Profiler(reporter, name(path));
    }

    /**
     * Counts the event identified by {@code metricId}
     */
    public void incrementCount(String metricId) {
        reporter.increment(metricId, 1L);
    }
    
    public void set(String metricId, Object itemId) {
        Preconditions.checkNotNull(metricId, "metricId");
        Preconditions.checkNotNull(itemId, "itemId");
        
        reporter.set(metricId, itemId.toString());
    }
    
    public void histogram(String metricId, double value) {
        Preconditions.checkNotNull(metricId, "metricId");
        
        reporter.histogram(metricId, value);
    }
}
