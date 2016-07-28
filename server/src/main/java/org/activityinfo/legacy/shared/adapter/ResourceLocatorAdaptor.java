package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gwt.core.shared.GWT;
import org.activityinfo.api.client.*;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.model.form.*;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservablePromise;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromiseExecutionOperation;
import org.activityinfo.promise.PromisesExecutionGuard;
import org.activityinfo.promise.PromisesExecutionMonitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Exposes a legacy {@code Dispatcher} implementation as new {@code ResourceLocator}
 */
public class ResourceLocatorAdaptor implements ResourceLocator {

    private ActivityInfoClientAsync client;

    public ResourceLocatorAdaptor() {
        if(!GWT.isClient()) {
            throw new IllegalStateException("Can only be called in client code.");
        }
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
    public Observable<ColumnSet> getTable(QueryModel queryModel) {
        return new ObservablePromise<>(client.queryTableColumns(queryModel));
    }

    @Override
    public Promise<ColumnSet> queryTable(QueryModel queryModel) {
        return client.queryTableColumns(queryModel);
    }

    @Override
    public Promise<FormInstance> getFormInstance(final ResourceId formId, final ResourceId formRecordId) {
        final Promise<FormClass> formClass = client.getFormSchema(formId.asString());
        final Promise<FormRecord> record = client.getRecord(formId.asString(), formRecordId.asString());

        return Promise.waitAll(formClass, record).then(new Function<Void, FormInstance>() {
            @Nullable
            @Override
            public FormInstance apply(@Nullable Void input) {
               return toFormInstance(formClass.get(), record.get());
            }
        });
    }

    @Override
    public Promise<List<FormInstance>> getSubFormInstances(ResourceId subFormId, ResourceId parentRecordId) {
        final Promise<FormRecordSet> records = client.getRecords(subFormId.asString(), parentRecordId.asString());
        final Promise<FormClass> subFormClass = getFormClass(subFormId);
        return Promise.waitAll(records, subFormClass).then(new Function<Void, List<FormInstance>>() {
            @Nullable
            @Override
            public List<FormInstance> apply(@Nullable Void aVoid) {
                List<FormInstance> instances = new ArrayList<FormInstance>();
                for (FormRecord record : records.get().getRecords()) {
                    instances.add(toFormInstance(subFormClass.get(), record));
                }
                return instances;
            }
        });
    }

    public FormInstance toFormInstance(FormClass formClass, FormRecord record) {
        FormInstance instance = new FormInstance(ResourceId.valueOf(record.getRecordId()), formClass.getId());
        for (FormField field : formClass.getFields()) {
            JsonElement fieldValue = record.getFields().get(field.getName());
            if(fieldValue != null && !fieldValue.isJsonNull()) {
                instance.set(field.getId(), field.getType().parseJsonValue(fieldValue));
            }
        }
        return instance;
    }


    @Override
    public Promise<Void> persist(IsResource resource) {
        if(resource instanceof FormClass) {
            return client.updateFormSchema(resource.getId().asString(), (FormClass)resource);

        } else if(resource instanceof FormInstance) {
            FormInstance instance = (FormInstance) resource;
            return client.createRecord(
                    instance.getClassId().asString(),
                    buildUpdate(instance))
                    .thenDiscardResult();
        }
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    private NewFormRecordBuilder buildUpdate(FormInstance instance) {
        NewFormRecordBuilder update = new NewFormRecordBuilder();
        update.setId(instance.getId().asString());
        update.setParentRecordId(instance.getOwnerId().asString());
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
        List<Promise<Void>> promises = Lists.newArrayList();
        for (IsResource resource : resources) {
            promises.add(persist(resource));
        }
        return Promise.waitAll(promises);
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
        if(criteria instanceof ClassCriteria) {
            return getFormClass(((ClassCriteria) criteria).getClassId()).join(new Function<FormClass, Promise<List<FormInstance>>>() {
                @Nullable
                @Override
                public Promise<List<FormInstance>> apply(@Nullable FormClass input) {
                    InstanceQueryAdapter adapter = new InstanceQueryAdapter();
                    QueryModel queryModel = adapter.build(input);

                    return client.queryTableColumns(queryModel).then(adapter.toFormInstances());
                }
            });
        } else {
            return Promise.rejected(new UnsupportedOperationException("criteria: " + criteria));
        }
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
    public Promise<List<CatalogEntry>> getCatalogEntries(String parentId) {
        return client.getFormCatalog(parentId);
    }

}
