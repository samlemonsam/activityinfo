package org.activityinfo.ui.client.store.offline;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.FormRecordUpdateBuilder;
import org.activityinfo.api.client.NewFormRecordBuilder;
import org.activityinfo.model.account.AccountStatus;
import org.activityinfo.model.analysis.Analysis;
import org.activityinfo.model.analysis.AnalysisUpdate;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OfflineClientStub implements ActivityInfoClientAsync {

    private boolean online = false;
    private List<RecordTransaction> transactionsSubmitted = new ArrayList<>();

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isOnline() {
        return online;
    }

    public List<RecordTransaction> getTransactionsSubmitted() {
        return transactionsSubmitted;
    }

    private <T> Promise<T> offline() {
        return Promise.rejected(new RuntimeException("offline"));
    }

    @Override
    public Promise<UserDatabaseMeta> getDatabase(ResourceId databaseId) {
        return offline();
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
    public Promise<RecordHistory> getRecordHistory(String formId, String recordId) {
        return offline();
    }

    @Override
    public Promise<FormRecordSet> getRecords(String formId, String parentId) {
        return offline();
    }

    @Override
    public Promise<FormSyncSet> getRecordVersionRange(String formId, long localVersion, long toVersion, Optional<String> cursor) {
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
    public Promise<Void> updateRecords(RecordTransaction transaction) {
        if(online) {
            transactionsSubmitted.add(transaction);
            return Promise.done();
        } else {
            return offline();
        }
    }

    @Override
    public Promise<Maybe<Analysis>> getAnalysis(String id) {
        return offline();
    }

    @Override
    public Promise<Void> updateAnalysis(AnalysisUpdate analysis) {
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

    @Override
    public Promise<Void> requestDatabaseTransfer(String s, int i) {
        return offline();
    }

    @Override
    public Promise<Void> cancelDatabaseTransfer(int i) {
        return offline();
    }

    @Override
    public Promise<List<FormMetadata>> getFormTreeList(ResourceId formId) {
        return offline();
    }

    @Override
    public Promise<AccountStatus> getAccountStatus() {
        return offline();
    }
}
