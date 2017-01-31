package org.activityinfo.ui.client.dispatch;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import org.activityinfo.api.client.*;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
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
               return FormInstance.toFormInstance(formClass.get(), record.get());
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
                List<FormInstance> instances = Lists.newArrayList();
                for (FormRecord record : records.get().getRecords()) {
                    instances.add(FormInstance.toFormInstance(subFormClass.get(), record));
                }
                return instances;
            }
        });
    }

    @Override
    public Promise<List<FormHistoryEntry>> getFormRecordHistory(ResourceId formId, ResourceId recordId) {
        return client.getRecordHistory(formId.asString(), recordId.asString());
    }


    @Override
    public Promise<Void> persist(FormInstance instance) {
        return client.createRecord(
                instance.getFormId().asString(),
                buildUpdate(instance))
                .thenDiscardResult();
    }

    @Override
    public Promise<Void> persist(FormClass formClass) {
        return client.updateFormSchema(formClass.getId().asString(), formClass);
    }

    private NewFormRecordBuilder buildUpdate(FormInstance instance) {
        NewFormRecordBuilder update = new NewFormRecordBuilder();
        update.setId(instance.getId().asString());
        update.setParentRecordId(instance.getParentRecordId().asString());

        for (Map.Entry<ResourceId, FieldValue> entry : instance.getFieldValueMap().entrySet()) {
            String field = entry.getKey().asString();
            if(!field.equals("classId")) {
                update.setFieldValue(field, entry.getValue().toJsonElement());
            }
        }
        return update;
    }

    @Override
    public Promise<Void> persist(List<FormInstance> formInstances) {
        return persist(formInstances, null);
    }

    @Override
    public Promise<Void> persist(List<FormInstance> formInstances, @Nullable PromisesExecutionMonitor monitor) {
        List<Promise<Void>> promises = Lists.newArrayList();
        for (FormInstance instance : formInstances) {
            promises.add(persist(instance));
        }
        return Promise.waitAll(promises);
    }

    @Override
    public Promise<Void> persistOperation(List<PromiseExecutionOperation> operations, @Nullable PromisesExecutionMonitor monitor) {
        return PromisesExecutionGuard.newInstance().withMonitor(monitor).executeSerially(operations);
    }

    public Promise<Void> remove(ResourceId formId, ResourceId resourceId) {
        FormRecordUpdateBuilder builder = new FormRecordUpdateBuilder();
        builder.setDeleted(true);

        return client.updateRecord(formId.asString(), resourceId.asString(), builder).thenDiscardResult();
    }

    @Override
    public Promise<Void> remove(ResourceId formId, Collection<ResourceId> resources) {
        // todo one call instead of multiple
        List<Promise<Void>> promises = Lists.newArrayList();
        for (ResourceId resourceId : resources) {
            promises.add(remove(formId, resourceId));
        }
        return Promise.waitAll(promises);
    }

    @Override
    public Promise<List<CatalogEntry>> getCatalogEntries(String parentId) {
        return client.getFormCatalog(parentId);
    }

}
