package org.activityinfo.ui.client.store;

import com.google.common.collect.Maps;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.http.CatalogRequest;
import org.activityinfo.ui.client.http.HttpBus;
import org.activityinfo.ui.client.http.QueryRequest;
import org.activityinfo.ui.client.http.RecordRequest;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class FormStoreImpl implements FormStore {

    private static final Logger LOGGER = Logger.getLogger(FormStoreImpl.class.getName());

    private HttpBus httpBus;
    private OfflineStore offlineStore;

    private Map<ResourceId, ObservableForm> formMap = Maps.newHashMap();

    public FormStoreImpl(HttpBus httpBus, OfflineStore offlineStore) {
        this.httpBus = httpBus;
        this.offlineStore = offlineStore;
    }

    @Override
    public Observable<FormClass> getFormClass(ResourceId formId) {
        ObservableForm form = formMap.get(formId);
        if(form == null) {
            form = new ObservableForm(httpBus, offlineStore, formId);
            formMap.put(formId, form);
        }
        return form;
    }

    @Override
    public Observable<FormTree> getFormTree(ResourceId rootFormId) {
        return new ObservableFormTree(rootFormId, this::getFormClass);
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
        return httpBus.get(new RecordRequest(recordRef));
    }

}
