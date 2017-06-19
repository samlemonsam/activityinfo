package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.TransactionBuilder;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.query.shared.FormSource;

import java.util.List;

public interface FormStore extends FormSource {


    Observable<FormMetadata> getFormMetadata(ResourceId formId);

    Promise<Void> deleteForm(ResourceId formId);

    Observable<List<CatalogEntry>> getCatalogRoots();

    Observable<List<CatalogEntry>> getCatalogChildren(ResourceId parentId);

    void setFormOffline(ResourceId formId, boolean offline);

    Observable<OfflineStatus> getOfflineStatus(ResourceId formId);

    /**
     * Applies an update transactionally to the Form store.
     */
    Promise<Void> updateRecords(TransactionBuilder transactionBuilder);

    <T extends JobDescriptor<R>, R extends JobResult> Observable<JobStatus<T, R>>  startJob(T job);
}
