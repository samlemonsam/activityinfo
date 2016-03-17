package org.activityinfo.core.client;


import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.AsyncFormClassProvider;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromiseExecutionOperation;
import org.activityinfo.promise.PromisesExecutionMonitor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ResourceLocator extends AsyncFormClassProvider {

    /**
     * Fetches the user form.
     *
     * @param formId
     * @return
     */
    Promise<FormClass> getFormClass(ResourceId formId);

    Promise<FormInstance> getFormInstance(ResourceId formId);

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

    Promise<QueryResult<FormInstance>> queryInstances(InstanceQuery criteria);

    /**
     * Retrieves the form instances that match the given criteria.
     * @param criteria
     */
    Promise<List<FormInstance>> queryInstances(Criteria criteria);

    Promise<QueryResult<Projection>> queryProjection(InstanceQuery query);

    Promise<List<Projection>> query(InstanceQuery query);

    Promise<Void> remove(ResourceId resourceId);

    Promise<Void> remove(Collection<ResourceId> resources);

    Promise<List<FormInstance>> queryInstances(Set<ResourceId> resourceIds);
}
