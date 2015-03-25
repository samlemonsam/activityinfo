package org.activityinfo.server.util.monitoring;


import java.util.concurrent.TimeUnit;

public class Profiler {
    
    private final long started;
    private final String prefix;
    private MetricsReporter reporter;

    Profiler(MetricsReporter reporter, String prefix) {
        this.reporter = reporter;
        this.prefix = prefix;
        this.started = System.nanoTime();
    }

    public void failed() {
        reporter.increment(prefix + ".failed", 1L);
        reportTime();
    }
    
    public void succeeded() {
        markSuccess();
        reportTime();
    }
    
    void succeeded(boolean time) {
        markSuccess();
        reportTime();
    }

    void reportTime() {
        reporter.time(prefix + ".time", elapsedTime());
    }

    void markSuccess() {
        reporter.increment(prefix + ".succeeded", 1L);
    }

    private long elapsedTime() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
    }
}
