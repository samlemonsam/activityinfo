/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.store.http;

import com.google.common.collect.Iterables;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Function2;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.TaskExecution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides serialization and retrying of HttpRequests
 */
public class HttpBus {

    private static final Logger LOGGER = Logger.getLogger(HttpBus.class.getName());


    private class PendingRequest<T> implements TaskExecution {
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
            return result != null && result.getState() == Promise.State.REJECTED;
        }

        @Override
        public boolean isRunning() {
            return !cancelled && (result == null || result.getState() != Promise.State.FULFILLED);
        }

        @Override
        public void cancel() {
            this.cancelled = true;
            onDone(this);
        }

        public boolean isSubmitted() {
            return result != null;
        }
    }


    private final ActivityInfoClientAsync client;
    private final Scheduler scheduler;
    private final List<PendingRequest<?>> pendingRequests = new ArrayList<>();

    /**
     * True if we have a connection, according to the browser.
     */
    private final Observable<Boolean> online;

    /**
     * True if there are pending requests
     */
    private final StatefulValue<Boolean> pending = new StatefulValue<>(false);

    /**
     * True if we currently fetching from the server.
     */
    private final StatefulValue<Boolean> fetching = new StatefulValue<>(false);

    /**
     * True if the connection is broken
     */
    private final StatefulValue<Boolean> broken = new StatefulValue<>(false);

    private RetryTask retryTask = new RetryTask();
    private int nextRequestId = 1;

    public HttpBus(ActivityInfoClientAsync client, Observable<Boolean> online, Scheduler scheduler) {
        this.client = client;
        this.scheduler = scheduler;
        this.online = online;
        this.online.subscribe(this::onConnectionChanged);
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
    public <T> TaskExecution submit(HttpRequest<T> request, AsyncCallback<T> callback) {

        PendingRequest<T> pending = new PendingRequest<>(request, callback);
        pendingRequests.add(pending);

        // Update our status that are now pending requests
        this.pending.updateIfNotEqual(true);

        // If we are online, submit the request immediately.
        if(online.isLoaded() && online.get()) {

            fetching.updateIfNotEqual(true);
            pending.execute();
        }

        return pending;
    }

    public Observable<Boolean> getFetchingStatus() {
        return fetching;
    }

    public Observable<Boolean> getPendingStatus() {
        return pending;
    }

    public Observable<Boolean> getOnline() {
        return Observable.transform(online, broken, new Function2<Boolean, Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean online, Boolean broken) {
                return online && !broken;
            }
        });
    }

    private void onConnectionChanged(Observable<Boolean> observable) {
        if(observable.isLoaded() && observable.get()) {
            submitNext();
        }
    }


    /**
     * Called when a request has failed due to a connection problem with the server.
     */
    private void onConnectionFailure(PendingRequest<?> request, Throwable caught) {

        LOGGER.severe("Request #" + request.id + " failed: " + caught.getMessage());



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
    private Iterable<PendingRequest<?>> failedOrUnsubmittedRequests() {
        List<PendingRequest<?>> failed = new ArrayList<>();
        for (PendingRequest<?> pendingRequest : pendingRequests) {
            if (!pendingRequest.isSubmitted() || pendingRequest.isFailed()) {
                failed.add(pendingRequest);
            }
        }
        return failed;
    }

    /**
     * Called when a request succeeds.
     */
    private void onSucceeded(PendingRequest<?> request) {
        LOGGER.info("Request #" + request.id + " succeeded.");

        broken.updateIfNotEqual(false);

        submitNext();
    }

    private void submitNext() {
        if (pendingRequests.isEmpty()) {
            fetching.updateIfNotEqual(false);
            pending.updateIfNotEqual(false);
        } else {
            for (PendingRequest<?> pendingRequest : pendingRequests) {
                if (!pendingRequest.isSubmitted() || pendingRequest.isFailed()) {
                    pendingRequest.execute();
                }
            }
        }
    }


    /**
     * Called when a request has succeeded or cancelled and should be removed from the queue.
     */
    private void onDone(PendingRequest<?> request) {
        pendingRequests.remove(request);
        if (pendingRequests.isEmpty()) {
            fetching.updateIfNotEqual(false);
            pending.updateIfNotEqual(false);
        }
    }

    /**
     * Retries the oldest request in the queue.
     */
    private class RetryTask implements Scheduler.RepeatingCommand {

        private boolean scheduled = false;

        @Override
        public boolean execute() {

            scheduled = false;

            Iterable<PendingRequest<?>> failedRequests = failedOrUnsubmittedRequests();

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
}
