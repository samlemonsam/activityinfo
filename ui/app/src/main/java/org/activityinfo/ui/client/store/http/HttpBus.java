package org.activityinfo.ui.client.store.http;


import com.google.common.collect.Iterables;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.TransactionBuilder;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.ObservableFormTree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages pendingRequests to the remote server, handling retries and other
 */
public class HttpBus {

    private static final Logger LOGGER = Logger.getLogger(HttpBus.class.getName());

    private int nextRequestId = 1;
    private Scheduler scheduler;


    private class PendingRequest<T> implements HttpSubscription {
        private int id = nextRequestId++;
        private final HttpRequest<T> request;
        private final AsyncCallback<T> callback;
        private Promise<T> result;

        private boolean cancelled = false;

        public PendingRequest(HttpRequest<T> request, AsyncCallback<T> callback) {
            this.request = request;
            this.callback = callback;
        }

        public void execute() {
            this.result = request.execute(client);
            this.result.then(new AsyncCallback<T>() {
                @Override
                public void onFailure(Throwable caught) {
                    if (!cancelled) {

                        onConnectionFailure(PendingRequest.this, caught);

                        // Normally we don't bother the requester with failure.
                        // We just keep re-trying periodically until it succeeds.
                    }
                }

                @Override
                public void onSuccess(T result) {
                    // We're done! Remove ourselves from the list and report back to the callback.
                    onDone(PendingRequest.this);
                    if (!cancelled) {

                        onSucceeded(PendingRequest.this);

                        callback.onSuccess(result);
                    }
                }
            });
        }

        private boolean isFailed() {
            return result.getState() == Promise.State.REJECTED;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
            onDone(this);
        }
    }


    private final ActivityInfoClientAsync client;
    private final List<PendingRequest<?>> pendingRequests = new ArrayList<>();
    private final StatefulValue<HttpStatus> status = new StatefulValue<>();

    private RetryTask retryTask = new RetryTask();

    public HttpBus(ActivityInfoClientAsync client) {
        this.client = client;
        scheduler = Scheduler.get();
    }

    public HttpBus(ActivityInfoClientAsync client, Scheduler scheduler) {
        this.client = client;
        this.scheduler = scheduler;
    }

    public Observable<HttpStatus> getStatus() {
        return status;
    }

    /**
     * Submits a new HTTP request to the queue.
     * <p>
     * <p>This bus will continue to retry the request until it succeeds or it is cancelled.
     *
     * @param request  the request to execute
     * @param callback the callback to receive the result.
     * @param <T>      the type of the result.
     * @return
     */
    public <T> HttpSubscription submit(HttpRequest<T> request, AsyncCallback<T> callback) {

        PendingRequest<T> pending = new PendingRequest<>(request, callback);
        pendingRequests.add(pending);

        pending.execute();

        status.updateValue(HttpStatus.FETCHING);

        return pending;
    }

    public <T> Observable<T> get(HttpRequest<T> request) {
        return new ObservableRequest<T>(this, request);
    }

    public Observable<FormMetadata> getFormMetadata(ResourceId formId) {
        return get(new FormMetadataRequest(formId));
    }


    public Promise<Void> updateRecords(TransactionBuilder transactionBuilder) {
        return client.updateRecords(transactionBuilder);

    }

    public Observable<FormTree> getFormTree(ResourceId rootFormId) {
        return new ObservableFormTree(rootFormId, this::getFormMetadata, scheduler);
    }

    /**
     * Called when a request has succeeded or cancelled and should be removed from the queue.
     */
    private void onDone(PendingRequest<?> request) {
        pendingRequests.remove(request);
        if (pendingRequests.isEmpty()) {
            status.updateValue(HttpStatus.IDLE);
        }
    }

    /**
     * Called when a request has failed due to a connection problem with the server.
     */
    private void onConnectionFailure(PendingRequest<?> request, Throwable caught) {

        LOGGER.severe("Request #" + request.id + " failed: " + caught.getMessage());

        status.updateValue(HttpStatus.BROKEN);

        // We want to keep retrying the request, but we don't want to trigger a stampede and
        // retry queued tasks all at once.
        if(!retryTask.scheduled) {
            retryTask.scheduled = true;
            scheduler.scheduleFixedDelay(retryTask, 2500);
        }
    }

    /**
     * @return sequence of pending requests that have failed.
     */
    private Iterable<PendingRequest<?>> failedRequests() {
        List<PendingRequest<?>> failed = new ArrayList<>();
        for (PendingRequest<?> pendingRequest : pendingRequests) {
            if (pendingRequest.isFailed()) {
                failed.add(pendingRequest);
            }
        }
        return failed;
    }

    /**
     * Retries the oldest request in the queue.
     */
    private class RetryTask implements Scheduler.RepeatingCommand {

        private boolean scheduled = false;

        @Override
        public boolean execute() {

            scheduled = false;

            Iterable<PendingRequest<?>> failedRequests = failedRequests();

            LOGGER.info("Retrying. Failed requests: " + Iterables.size(failedRequests));

            // When retrying, attempt only one request at a time until we have some success.
            Iterator<PendingRequest<?>> requestIt = failedRequests.iterator();
            if (requestIt.hasNext()) {
                PendingRequest<?> request = requestIt.next();
                LOGGER.info("Retrying request #" + request.id + "...");
                request.execute();
            }

            // Do not repeat. will be rescheduled as neccessary
            // if the connection fails again.
            return false;
        }
    }


    /**
     * Called when a request succeeds.
     */
    private void onSucceeded(PendingRequest<?> request) {
        LOGGER.info("Request #" + request.id + " succeeded.");

        if (pendingRequests.isEmpty()) {
            status.updateValue(HttpStatus.IDLE);
        } else {
            for (PendingRequest<?> pendingRequest : pendingRequests) {
                if (pendingRequest.isFailed()) {
                    pendingRequest.execute();
                }
            }
        }
    }
}

