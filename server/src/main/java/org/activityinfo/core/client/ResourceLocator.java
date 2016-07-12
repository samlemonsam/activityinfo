package org.activityinfo.core.client;


import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.AsyncFormClassProvider;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromiseExecutionOperation;
import org.activityinfo.promise.PromisesExecutionMonitor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ResourceLocator extends AsyncFormClassProvider {

    /**
     * Fetches the schema of a user's forms. 
     * 
     * <p>Convenience method for {@code FormClass.fromResource(fetchResource(formId))}</p>
     *
     * @param formId
     * @return
     */
    Promise<FormClass> getFormClass(ResourceId formId);

    /**
     * Fetches a single resource.
     * @param resourceId the id of the resource to fetch
     */
    Observable<Resource> fetchResource(ResourceId resourceId);

    /**
     * Queries a flat, two dimensional view of collections.
     * 
     * @param queryModel model describing the query.
     */
    Observable<ColumnSet> queryTable(QueryModel queryModel);
    
    @Deprecated
    Promise<FormInstance> getFormInstance(ResourceId formId, ResourceId formRecordId);

    Promise<List<FormInstance>> getSubFormInstances(ResourceId subFormId, ResourceId parentRecordId);
    
    /**
     * Persists a resource to the server, creating or updating as necessary.
     *
     * @param resource the resource to persist.
     * @return a Promise that resolves when the persistance operation completes
     * successfully.
     */
    Promise<Void> persist(IsResource resource);

    Promise<Void> persist(List<? extends IsResource> resources);

    Promise<Void> persist(List<? extends IsResource> resources, @Nullable PromisesExecutionMonitor monitor);

    Promise<Void> persistOperation(List<PromiseExecutionOperation> operations);

    Promise<Void> persistOperation(List<PromiseExecutionOperation> resources, @Nullable PromisesExecutionMonitor monitor);

    /**
     * 
     * @param criteria
     * @deprecated use {@link #queryTable(ColumnModel)}
     */
    @Deprecated
    Promise<QueryResult<FormInstance>> queryInstances(InstanceQuery criteria);

    /**
     * Retrieves the form instances that match the given criteria.
     * @param criteria
     * @deprecated use {@link #queryTable(ColumnModel)}
     */
    @Deprecated
    Promise<List<FormInstance>> queryInstances(Criteria criteria);

    /**
     * 
     * @param query
     * @deprecated use {@link #queryTable(ColumnModel)}
     */
    @Deprecated
    Promise<QueryResult<Projection>> queryProjection(InstanceQuery query);

    /**
     * 
     * @param query
     * @deprecated use {@link #queryTable(ColumnModel)}
     */
    @Deprecated
    Promise<List<Projection>> query(InstanceQuery query);

    Promise<Void> remove(ResourceId formId, ResourceId resourceId);

    Promise<Void> remove(ResourceId formId, Collection<ResourceId> resources);

    /**
     * @param resourceIds
     * @deprecated use {@link #queryTable(ColumnModel)}
     */
    @Deprecated
    Promise<List<FormInstance>> queryInstances(Set<ResourceId> resourceIds);
}
