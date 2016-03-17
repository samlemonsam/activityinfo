package org.activityinfo.observable;

import java.util.ArrayList;
import java.util.List;


public class SchedulerStub implements Scheduler {
    
    private List<Runnable> queue = new ArrayList<>();

    @Override
    public void schedule(Runnable runnable) {
        queue.add(runnable);
    }

    public void runAll() {
        for (Runnable task : queue) {
            task.run();
        }
        queue.clear();
    }
}
