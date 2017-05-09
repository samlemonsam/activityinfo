package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.Scheduler;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.http.*;
import org.activityinfo.ui.client.store.offline.OfflineStore;

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
        Observable<FormMetadata> online = httpBus.get(new FormMetadataRequest(formId));
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
        return httpBus.get(new QueryRequest(queryModel));
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
    public Observable<FormRecord> getRecord(RecordRef recordRef) {
        Observable<FormRecord> online = httpBus.get(new RecordRequest(recordRef));
        Observable<FormRecord> offline = offlineStore.getCachedRecord(recordRef);

        return new Best<>(online, offline, (x, y) -> 0);
    }

}
