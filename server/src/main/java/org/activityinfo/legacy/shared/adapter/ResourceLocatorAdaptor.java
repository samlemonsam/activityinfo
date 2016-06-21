package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.ActivityInfoClientAsyncImpl;
import org.activityinfo.api.client.FormRecordUpdateBuilder;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservablePromise;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromiseExecutionOperation;
import org.activityinfo.promise.PromisesExecutionGuard;
import org.activityinfo.promise.PromisesExecutionMonitor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exposes a legacy {@code Dispatcher} implementation as new {@code ResourceLocator}
 */
public class ResourceLocatorAdaptor implements ResourceLocator {

    private ActivityInfoClientAsync client;

    public ResourceLocatorAdaptor() {
        this.client = new ActivityInfoClientAsyncImpl();
    }

    public ResourceLocatorAdaptor(ActivityInfoClientAsync client) {
        this.client = client;
    }

    @Override
    public Promise<FormClass> getFormClass(ResourceId classId) {
        return client.getFormSchema(classId.asString());
    }

    @Override
    public Observable<Resource> fetchResource(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<ColumnSet> queryTable(QueryModel queryModel) {
        return new ObservablePromise<>(client.queryTableColumns(queryModel));
    }

    @Override
    public Promise<FormInstance> getFormInstance(ResourceId formId, ResourceId formRecordId) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<Void> persist(IsResource resource) {
        if(resource instanceof FormClass) {
            return client.updateFormSchema(resource.getId().asString(), (FormClass)resource);

        } else if(resource instanceof FormInstance) {
            FormInstance instance = (FormInstance) resource;
            return client.updateRecord(
                    instance.getClassId().asString(), 
                    instance.getId().asString(),
                    buildUpdate(instance))
                    .thenDiscardResult();
        }
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    private FormRecordUpdateBuilder buildUpdate(FormInstance instance) {
        FormRecordUpdateBuilder update = new FormRecordUpdateBuilder();
        for (Map.Entry<ResourceId, FieldValue> entry : instance.getFieldValueMap().entrySet()) {
            String field = entry.getKey().asString();
            if(!field.equals("classId")) {
                update.setFieldValue(field, entry.getValue().toJsonElement());
            }
        }
        return update;
    }

    @Override
    public Promise<Void> persist(List<? extends IsResource> resources) {
        return persist(resources, null);
    }

    @Override
    public Promise<Void> persist(List<? extends IsResource> resources, @Nullable PromisesExecutionMonitor monitor) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<Void> persistOperation(List<PromiseExecutionOperation> operations) {
        return persistOperation(operations, null);
    }

    @Override
    public Promise<Void> persistOperation(List<PromiseExecutionOperation> operations, @Nullable PromisesExecutionMonitor monitor) {
        return PromisesExecutionGuard.newInstance().withMonitor(monitor).executeSerially(operations);
    }

    public Promise<QueryResult<FormInstance>> queryInstances(InstanceQuery criteria) {
        return queryInstances(criteria.getCriteria()).then(new InstanceQueryResultAdapter<FormInstance>(criteria));
    }

    @Override
    public Promise<List<FormInstance>> queryInstances(Criteria criteria) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<List<Projection>> query(final InstanceQuery query) {
        final InstanceQueryAdapter adapter = new InstanceQueryAdapter();
        QueryModel queryModel = adapter.build(query);

        return client.queryTableColumns(queryModel).then(new Function<ColumnSet, List<Projection>>() {
            @Nullable
            @Override
            public List<Projection> apply(@Nullable ColumnSet input) {
                return adapter.toProjections(input);
            }
        });
    }

    @Override
    public Promise<QueryResult<Projection>> queryProjection(InstanceQuery query) {
        return query(query).then(new InstanceQueryResultAdapter(query));
    }

    public Promise<Void> remove(ResourceId formId, ResourceId resourceId) {
        FormRecordUpdateBuilder builder = new FormRecordUpdateBuilder();
        builder.setDeleted(true);

        return client.updateRecord(formId.asString(), resourceId.asString(), builder).thenDiscardResult();
    }

    @Override
    public Promise<Void> remove(ResourceId formId, Collection<ResourceId> resources) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<List<FormInstance>> queryInstances(Set<ResourceId> formClassIds) {
        return queryInstances(ClassCriteria.union(formClassIds));
    }
}
