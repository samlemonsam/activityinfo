package org.activityinfo.test.capacity.agent;

import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import org.activityinfo.test.capacity.Metrics;
import org.activityinfo.test.driver.UiApplicationDriver;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;

public class SyncOffline implements Runnable {

    private final Logger LOGGER = Logger.getLogger(SyncOffline.class.getName());

    private final Timer latency = Metrics.REGISTRY.timer(name(SyncOffline.class, "offlineSync"));

    private Agent agent;

    public SyncOffline(Agent agent) {
        Preconditions.checkNotNull(agent);
        this.agent = agent;
    }

    @Override
    public void run() {

        agent.startBrowserSession(new BrowserSession() {
            @Override
            public void execute(Agent agent, UiApplicationDriver applicationDriver) {
                LOGGER.info(String.format("%s [%s] is enabling offline mode...", 
                        agent.getName(), agent.getAccount().getEmail()));
                final Stopwatch stopwatch = Stopwatch.createStarted();
                try {
                    applicationDriver.loadOfflineMode();

                } finally {
                    stopwatch.stop();
                }

                latency.update(stopwatch.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
                LOGGER.info(String.format("Offline synchronization completed in %d seconds",
                        stopwatch.elapsed(TimeUnit.SECONDS)));
            }
        });
    }
}
