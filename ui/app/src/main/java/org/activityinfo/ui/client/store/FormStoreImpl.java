package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.Scheduler;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.TransactionBuilder;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.http.CatalogRequest;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.http.RecordRequest;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.activityinfo.ui.client.store.offline.SnapshotStatus;

import java.util.List;
import java.util.logging.Logger;


public class FormStoreImpl implements FormStore {

    private static final Logger LOGGER = Logger.getLogger(FormStoreImpl.class.getName());

    private final HttpBus httpBus;
    private final OfflineStore offlineStore;
    private final Scheduler scheduler;

    public FormStoreImpl(HttpBus httpBus, OfflineStore offlineStore, Scheduler scheduler) {
        this.httpBus = httpBus;
        this.offlineStore = offlineStore;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<FormMetadata> getFormMetadata(ResourceId formId) {
        Observable<FormMetadata> online = httpBus.getFormMetadata(formId);
        Observable<FormMetadata> offline = offlineStore.getCachedMetadata(formId);

        return new Best<>(online, offline, (x, y) -> Long.compare(x.getVersion(), y.getVersion()));
    }

    @Override
    public Promise<Void> deleteForm(ResourceId formId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Observable<FormTree> getFormTree(ResourceId rootFormId) {
        return new ObservableFormTree(rootFormId, this::getFormMetadata, scheduler);
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        return httpBus.query(queryModel);
    }

    @Override
    public Observable<List<CatalogEntry>> getCatalogRoots() {
        return httpBus.get(new CatalogRequest());
    }

    @Override
    public Observable<List<CatalogEntry>> getCatalogChildren(ResourceId parentId) {
        return httpBus.get(new CatalogRequest(parentId));
    }

    @Override
    public Observable<Maybe<FormRecord>> getRecord(RecordRef recordRef) {
        Observable<Maybe<FormRecord>> online = httpBus.get(new RecordRequest(recordRef));


//        Observable<Maybe<FormRecord>> offline = offlineStore.getCachedRecord(recordRef);
//        return new Best<>(online, offline, (x, y) -> 0);

        return online;
    }

    @Override
    public void setFormOffline(ResourceId formId, boolean offline) {
        offlineStore.enableOffline(formId, offline);
    }

    @Override
    public Observable<OfflineStatus> getOfflineStatus(ResourceId formId) {
        Observable<Boolean> enabled = offlineStore.getOfflineForms().transform(set -> set.contains(formId));
        Observable<SnapshotStatus> snapshot = offlineStore.getCurrentSnapshot();

        return Observable.transform(enabled, snapshot, (e, s) -> new OfflineStatus(e, s.isFormCached(formId)));
    }

    @Override
    public Promise<Void> updateRecords(TransactionBuilder transactionBuilder) {
        return httpBus.updateRecords(transactionBuilder);
    }

    @Override
    public <T extends JobDescriptor<R>, R extends JobResult> Observable<JobStatus<T, R>> startJob(T job) {
        return httpBus.startJob(job);
    }

}
