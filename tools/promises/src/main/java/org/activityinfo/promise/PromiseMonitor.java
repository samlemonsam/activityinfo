package org.activityinfo.promise;

/**
 * Interface for a generally UI component that is listening for
 * the status of the Promise
 */
public interface PromiseMonitor {

    void onPromiseStateChanged(Promise.State state);
}
