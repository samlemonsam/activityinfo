package org.activityinfo.test.capacity.load;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import org.joda.time.Duration;
import org.joda.time.Period;

import java.util.concurrent.TimeUnit;

public class LoadProfile {

    private Duration testDuration = Duration.standardMinutes(1);
    private Function<? super Duration, Integer> maxConcurrentRequests;
    private long maxRequestCount = Long.MAX_VALUE;

    public LoadProfile setTestDuration(Duration testDuration) {
        this.testDuration = testDuration;
        return this;
    }

    public LoadProfile setTestDuration(Period period) {
        this.testDuration = period.toStandardDuration();
        return this;
    }

    public LoadProfile setMaxConcurrentRequests(Function<? super Duration, Integer> maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
        return this;
    }

    public LoadProfile setMaxRequestCount(long count) {
        this.maxRequestCount = count;
        return this;
    }

    /**
     *
     * @param stopwatch a running stopwatch that was started with the beginning of the run
     * @return true if the run should be finished
     */
    public boolean isFinished(Stopwatch stopwatch, long requestCount) {
        return stopwatch.elapsed(TimeUnit.MILLISECONDS) > testDuration.getMillis() ||
                requestCount > maxRequestCount;

    }

    public int getMaxConcurrentRequests(Stopwatch stopwatch) {
        return maxConcurrentRequests.apply(Duration.millis(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }
}