package org.activityinfo.geoadmin.merge2.view.swing;

import org.activityinfo.observable.Scheduler;

import java.util.concurrent.ExecutorService;

/**
 * Created by alex on 27-5-15.
 */
public class SwingSchedulers {
    
    public static Scheduler fromExecutor(final ExecutorService executorService) {
        return new Scheduler() {
            @Override
            public void schedule(Runnable runnable) {
                executorService.execute(runnable);
            }
        };
    }
    
    
}
