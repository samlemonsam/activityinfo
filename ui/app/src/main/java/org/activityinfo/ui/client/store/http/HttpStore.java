package org.activityinfo.ui.client.store.http;


import com.google.common.base.Function;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.FormRecordSet;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobState;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;
import org.activityinfo.ui.client.store.FormChangeEvent;
import org.activityinfo.ui.client.store.ObservableFormTree;
import org.activityinfo.ui.client.store.tasks.NullWatcher;
import org.activityinfo.ui.client.store.tasks.ObservableTask;
import org.activityinfo.ui.client.store.tasks.Watcher;

import java.util.logging.Logger;

/**
 * Manages pendingRequests to the remote server, handling retries and other
 */
public class HttpStore {

    private static final Logger LOGGER = Logger.getLogger(HttpStore.class.getName());

    private Scheduler scheduler;
    private final ActivityInfoClientAsync client;
    private EventBus eventBus = new SimpleEventBus();

    private Observable<Boolean> online;
    private final HttpBus httpBus;


    public HttpStore(Observable<Boolean> online, ActivityInfoClientAsync client, Scheduler scheduler) {
        this.online = online;
        this.client = client;
        this.scheduler = scheduler;
        httpBus = new HttpBus(client, online, scheduler);
    }

    public HttpStore(ActivityInfoClientAsync client) {
        this(Observable.just(true), client, Scheduler.get());
    }

    public HttpStore(ActivityInfoClientAsync client, Scheduler scheduler) {
        this(Observable.just(true), client, scheduler);
    }

    public Observable<Boolean> isOnline() {
        return online;
    }

    public HttpBus getHttpBus() {
        return httpBus;
    }

    public <T> Observable<T> get(HttpRequest<T> request) {
        return get(request, NullWatcher.INSTANCE);
    }

    private <T> Observable<T> get(HttpRequest<T> request, Watcher watcher) {
        return new ObservableTask<T>(new RequestTask<T>(httpBus, request), watcher);
    }

    public <T extends JobDescriptor<R>, R extends JobResult> Observable<JobStatus<T, R>> startJob(T job) {

        Observable<JobStatus<T, R>> jobCreated = get(new StartJobRequest<>(job));

        return jobCreated.join(initialStatus -> {
            if(initialStatus.getState() == JobState.FAILED) {
                return Observable.just(initialStatus);
            } else {
                return get(new JobStatusRequest<T, R>(initialStatus.getId()));
            }
        });
    }

    public Observable<FormMetadata> getFormMetadata(ResourceId formId) {

        // We consider the version range request to be immutable, as old versions
        // don't change, so refeching shouldn't be necessary
        return get(new FormMetadataRequest(formId));
    }

    public Observable<ColumnSet> query(QueryModel queryModel) {
        return get(new QueryRequest(queryModel), new FormChangeWatcher(eventBus, change -> true));
    }


    public Observable<FormRecordSet> getVersionRange(ResourceId formId, long localVersion, long version) {
        return get(new VersionRangeRequest(formId, localVersion, version));
    }

    public Observable<Maybe<FormRecord>> getRecord(RecordRef ref) {
        return get(new RecordRequest(ref), new FormChangeWatcher(eventBus, change -> change.isRecordChanged(ref)));
    }

    public Promise<Void> updateRecords(RecordTransaction tx) {
        return client.updateRecords(tx).then(new Function<Void, Void>() {
            @Override
            public Void apply(Void aVoid) {
                eventBus.fireEvent(new FormChangeEvent(FormChange.from(tx)));
                return null;
            }
        });
    }

    public Observable<FormTree> getFormTree(ResourceId rootFormId) {
        return new ObservableFormTree(rootFormId, this::getFormMetadata, scheduler);
    }

}

