package org.activityinfo.ui.client.dispatch;


import org.activityinfo.api.client.FormHistoryEntry;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromiseExecutionOperation;
import org.activityinfo.promise.PromisesExecutionMonitor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface ResourceLocator {

    /**
     * Fetches the schema of a user's forms. 
     * 
     * @param formId
     * @return
     */
    Promise<FormClass> getFormClass(ResourceId formId);

    Promise<FormTree> getFormTree(ResourceId formId);


    /**
     * Queries a flat, two dimensional view of collections.
     * 
     * @param queryModel model describing the query.
     */
    Observable<ColumnSet> getTable(QueryModel queryModel);
    
    Promise<ColumnSet> queryTable(QueryModel queryModel);
    
    Promise<FormInstance> getFormInstance(ResourceId formId, ResourceId formRecordId);

    Promise<List<FormInstance>> getSubFormInstances(ResourceId subFormId, ResourceId parentRecordId);
    
    Promise<List<FormHistoryEntry>> getFormRecordHistory(ResourceId formId, ResourceId recordId);
    
    /**
     * Persists a resource to the server, creating or updating as necessary.
     *
     * @param resource the resource to persist.
     * @return a Promise that resolves when the persistance operation completes
     * successfully.
     */
    Promise<Void> persist(FormInstance resource);

    Promise<Void> persist(FormClass formClass);
    
    Promise<Void> persist(List<FormInstance> formInstances);

    Promise<Void> persist(List<FormInstance> formInstances, @Nullable PromisesExecutionMonitor monitor);

    Promise<Void> persistOperation(List<PromiseExecutionOperation> resources, @Nullable PromisesExecutionMonitor monitor);

    Promise<Void> remove(ResourceId formId, ResourceId resourceId);

    Promise<Void> remove(ResourceId formId, Collection<ResourceId> resources);
    
    Promise<List<CatalogEntry>> getCatalogEntries(String parentId);
    
}
