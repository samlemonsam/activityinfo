package org.activityinfo.ui.client.store;

import com.google.common.base.Optional;
import org.activityinfo.api.client.*;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.VersionedFormStorage;
import org.activityinfo.store.testing.TestingCatalog;

import java.util.List;

public class AsyncClientStub implements ActivityInfoClientAsync {

    private TestingCatalog catalog;
    private boolean connected = true;

    public AsyncClientStub() {
        this.catalog = new TestingCatalog();
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public Promise<List<CatalogEntry>> getFormCatalog(String parent) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<FormRecord> getRecord(String formId, String recordId) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<Void> updateRecord(String formId, String recordId, FormRecordUpdateBuilder update) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<List<FormHistoryEntry>> getRecordHistory(String formId, String recordId) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<FormRecordSet> getRecords(String formId, String parentId) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<FormRecordSet> getRecordVersionRange(String formId, long localVersion, long toVersion) {
        if(!connected) {
            return offlineResult();
        }
        Optional<FormStorage> form = catalog.getForm(ResourceId.valueOf(formId));
        if(!form.isPresent()) {
            return Promise.rejected(new RuntimeException("No such form"));
        }
        VersionedFormStorage formStorage = (VersionedFormStorage) form.get();
        return Promise.resolved(new FormRecordSet(formStorage.getVersionRange(localVersion, toVersion)));
    }

    @Override
    public Promise<Void> createRecord(String formId, NewFormRecordBuilder newRecord) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<FormClass> getFormSchema(String formId) {
        if(!connected) {
            return offlineResult();
        }

        Optional<FormStorage> formSchema = catalog.getForm(ResourceId.valueOf(formId));
        if(formSchema.isPresent()) {
            return Promise.resolved(formSchema.get().getFormClass());
        } else {
            return Promise.rejected(new UnsupportedOperationException("No such form: " + formId));
        }
    }

    @Override
    public Promise<FormMetadata> getFormMetadata(String formId) {
        if(!connected) {
            return offlineResult();
        }

        FormMetadata metadata = new FormMetadata();
        metadata.setId(ResourceId.valueOf(formId));
        metadata.setVersion(1);
        metadata.setSchemaVersion(1);

        Optional<FormStorage> form = catalog.getForm(ResourceId.valueOf(formId));
        if(!form.isPresent()) {
            metadata.setDeleted(false);
        } else {
            metadata.setSchema(form.get().getFormClass());
        }
        return Promise.resolved(metadata);
    }

    @Override
    public Promise<Void> updateFormSchema(String formId, FormClass updatedSchema) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    private <T> Promise<T> offlineResult() {
        return Promise.rejected(new RuntimeException("Offline"));
    }

}
