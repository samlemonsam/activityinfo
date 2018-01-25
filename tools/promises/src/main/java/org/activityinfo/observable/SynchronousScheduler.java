package org.activityinfo.observable;


public enum SynchronousScheduler implements Scheduler {
    
    INSTANCE;
    
    @Override
    public void schedule(Runnable runnable) {
        runnable.run();
    }
}
