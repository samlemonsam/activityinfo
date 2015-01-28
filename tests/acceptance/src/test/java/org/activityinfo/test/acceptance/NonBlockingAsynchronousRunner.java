package org.activityinfo.test.acceptance;

import org.junit.runners.model.RunnerScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;


public class NonBlockingAsynchronousRunner implements RunnerScheduler {

    private final List<Future<Object>> futures = Collections.synchronizedList(new ArrayList<Future<Object>>());
    private final ExecutorService fService;

    public NonBlockingAsynchronousRunner() {
        String threads = System.getProperty("junit.parallel.threads", "16");
        int numThreads = Integer.parseInt(threads);
        fService = Executors.newFixedThreadPool(numThreads);
    }

    public void schedule(final Runnable childStatement) {
        final Callable<Object> objectCallable = new Callable<Object>() {
            public Object call() throws Exception {
                childStatement.run();
                return null;
            }
        };
        futures.add(fService.submit(objectCallable));
    }

    public void finished() {
        waitForCompletion();
    }

    public void waitForCompletion() {
        for (Future<Object> each : futures)
            try {
                each.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
    }
}
