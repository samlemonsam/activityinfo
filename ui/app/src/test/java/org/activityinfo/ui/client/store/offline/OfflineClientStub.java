package org.activityinfo.ui.client.store.offline;

import org.activityinfo.api.client.*;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;

import java.util.List;

public class OfflineClientStub implements ActivityInfoClientAsync {

    private <T> Promise<T> offline() {
        return Promise.rejected(new RuntimeException("offline"));
    }

    @Override
    public Promise<List<CatalogEntry>> getFormCatalog(String parent) {
        return offline();
    }

    @Override
    public Promise<Maybe<FormRecord>> getRecord(String formId, String recordId) {
        return offline();
    }

    @Override
    public Promise<Void> updateRecord(String formId, String recordId, FormRecordUpdateBuilder update) {
        return offline();
    }

    @Override
    public Promise<List<FormHistoryEntry>> getRecordHistory(String formId, String recordId) {
        return offline();
    }

    @Override
    public Promise<FormRecordSet> getRecords(String formId, String parentId) {
        return offline();
    }

    @Override
    public Promise<FormRecordSet> getRecordVersionRange(String formId, long localVersion, long toVersion) {
        return offline();
    }

    @Override
    public Promise<Void> createRecord(String formId, NewFormRecordBuilder newRecord) {
        return offline();
    }

    @Override
    public Promise<FormClass> getFormSchema(String formId) {
        return offline();
    }

    @Override
    public Promise<FormMetadata> getFormMetadata(String formId) {
        return offline();

    }

    @Override
    public Promise<FormTree> getFormTree(ResourceId formId) {
        return offline();
    }

    @Override
    public Promise<Void> updateFormSchema(String formId, FormClass updatedSchema) {
        return offline();
    }

    @Override
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {
        return offline();
    }

    @Override
    public Promise<Void> updateRecords(RecordTransaction transactions) {
        return offline();
    }

    @Override
    public <T extends JobDescriptor<R>, R extends JobResult> Promise<JobStatus<T, R>> startJob(T job) {
        return offline();
    }

    @Override
    public Promise<JobStatus<?, ?>> getJobStatus(String jobId) {
        return offline();
    }
}
