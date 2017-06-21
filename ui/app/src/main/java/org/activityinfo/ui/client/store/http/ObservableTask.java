package org.activityinfo.ui.client.store.http;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.observable.Observable;


/**
 * Long running task whose result is observable.
 *
 */
class ObservableTask<T> extends Observable<T> {

    private final Task<T> task;
    private final Watcher watcher;

    private TaskExecution execution;
    private T result = null;
    private boolean connected = false;


    public ObservableTask(Task<T> task, Watcher watcher) {
        super();
        this.task = task;
        this.watcher = watcher;
    }

    @Override
    protected void onConnect() {

        connected = true;

        if(result == null) {
            startExecution();
        }

        watcher.start(() -> start());
    }


    /**
     *
     * @return true if a task execution is currently running.
     */
    private boolean isRunning() {
        if(execution == null) {
            return false;
        }
        return execution.isRunning();

    }

    /**
     * Starts a new execution of our task.
     */
    private void startExecution() {

        execution = task.start(new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(T result) {
                onExecutionComplete(result);
            }
        });
    }

    /**
     * Called when an execution of the task completes successfully.
     */
    private void onExecutionComplete(T result) {
        ObservableTask.this.result = result;
        ObservableTask.this.execution = null;
        ObservableTask.this.fireChange();

        maybeSchedulePoll(result);
    }

    private void maybeSchedulePoll(T result) {

        if(!connected) {
            return;
        }

        int refreshInterval = task.refreshInterval(result);
        if(refreshInterval > 0) {
            com.google.gwt.core.client.Scheduler.get().scheduleFixedDelay(() -> {
                start();
                return false;
            }, refreshInterval);
        }

    }

    /**
     * When the user's activity on the client side has affected the remote state,
     * for example, when an update is submitted, then we have to refetch the result.
     */
    private void start() {
        if(!connected) {
            return;
        }

        result = null;
        fireChange();
        startExecution();
    }

    @Override
    protected void onDisconnect() {

        connected = false;

        // If a task execution is still running, cancel it.
        if(execution != null) {
            execution.cancel();
            execution = null;
        }

        // Stop any external watching...
        watcher.stop();
    }

    @Override
    public boolean isLoading() {
        return result == null;
    }


    @Override
    public T get() {
        assert !isLoading();
        return result;
    }
}
