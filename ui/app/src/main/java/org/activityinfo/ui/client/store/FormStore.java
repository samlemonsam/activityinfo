package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.store.offline.FormOfflineStatus;

import java.util.List;

public interface FormStore extends FormSource {

    Observable<FormMetadata> getFormMetadata(ResourceId formId);

    Observable<RecordTree> getRecordTree(RecordRef rootRecordId);

    Promise<Void> deleteForm(ResourceId formId);

    Observable<List<CatalogEntry>> getCatalogRoots();

    Observable<List<CatalogEntry>> getCatalogChildren(ResourceId parentId);

    void setFormOffline(ResourceId formId, boolean offline);

    Observable<FormOfflineStatus> getOfflineStatus(ResourceId formId);

    /**
     * Applies an update transactionally to the Form store.
     */
    Promise<Void> updateRecords(RecordTransaction tx);

    <T extends JobDescriptor<R>, R extends JobResult> Observable<JobStatus<T, R>>  startJob(T job);
}
