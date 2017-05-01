package org.activityinfo.ui.client.store;

import com.google.common.base.Optional;
import org.activityinfo.api.client.*;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.spi.FormStorage;
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
    public Promise<Void> createRecord(String formId, NewFormRecordBuilder newRecord) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<FormClass> getFormSchema(String formId) {

        if(!connected) {
            return Promise.rejected(new RuntimeException("Offline"));
        }

        Optional<FormStorage> formSchema = catalog.getForm(ResourceId.valueOf(formId));
        if(formSchema.isPresent()) {
            return Promise.resolved(formSchema.get().getFormClass());
        } else {
            return Promise.rejected(new UnsupportedOperationException("No such form: " + formId));
        }
    }

    @Override
    public Promise<Void> updateFormSchema(String formId, FormClass updatedSchema) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {
        return Promise.rejected(new UnsupportedOperationException());
    }
}
