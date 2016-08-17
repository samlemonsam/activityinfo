package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import org.activityinfo.api.client.*;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormCatalog;
import org.activityinfo.store.hrd.HrdFormAccessor;
import org.activityinfo.store.mysql.MySqlCatalog;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.Updater;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Collections;
import java.util.List;

public class ActivityInfoClientAsyncStub implements ActivityInfoClientAsync {

    private Provider<EntityManager> entityManager;

    @Inject
    public ActivityInfoClientAsyncStub(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    private FormCatalog newCatalog() {

        // Reset the current transaction
        if(entityManager.get().getTransaction().isActive()) {
            entityManager.get().getTransaction().commit();
        }

        // Create a fresh catalog to simulate a new request
        return new MySqlCatalog(new HibernateQueryExecutor(entityManager));
    }

    @Override
    public Promise<FormClass> getFormSchema(String formId) {
        FormCatalog catalog = newCatalog();

        FormClass formClass;
        try {
            formClass = defensiveCopy(catalog.getFormClass(ResourceId.valueOf(formId)));
        } catch (Exception e) {
            return Promise.rejected(e);
        }
        return Promise.resolved(formClass);
    }


    @Override
    public Promise<Void> updateFormSchema(String formId, FormClass updatedSchema) {
        FormCatalog catalog = newCatalog();

        try {
            EntityTransaction tx = entityManager.get().getTransaction();
            tx.begin();

            Optional<FormAccessor> collection = catalog.getForm(updatedSchema.getId());
            if(collection.isPresent()) {
                collection.get().updateFormClass(updatedSchema);
            } else {
                ((MySqlCatalog) catalog).createOrUpdateFormSchema(updatedSchema);
            }

            tx.commit();

            return Promise.resolved(null);

        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    private FormClass defensiveCopy(FormClass formClass) {
        return FormClass.fromResource(formClass.asResource());
    }


    @Override
    public Promise<List<CatalogEntry>> getFormCatalog(String parent) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<FormRecord> getRecord(String formId, String recordId) {
        try {
            FormCatalog catalog = newCatalog();
            Optional<FormAccessor> collection = catalog.getForm(ResourceId.valueOf(formId));
            if(!collection.isPresent()) {
                throw new RuntimeException("No such form " + formId);
            }
            Optional<FormRecord> record = collection.get().get(ResourceId.valueOf(recordId));
            if(!record.isPresent()) {
                throw new RuntimeException("No such recod " + recordId + " in form " + formId);
            }
            return Promise.resolved(record.get());

        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<Void> updateRecord(String formId, String recordId, FormRecordUpdateBuilder query) {
        try {
            FormCatalog catalog = newCatalog();
            Updater updater = new Updater(catalog);
            updater.execute(ResourceId.valueOf(formId), ResourceId.valueOf(recordId), query.toJsonObject());

            return Promise.resolved(null);
            
        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<List<FormHistoryEntry>> getRecordHistory(String formId, String recordId) {
        return Promise.resolved(Collections.<FormHistoryEntry>emptyList());
    }

    @Override
    public Promise<Void> createRecord(String formId, NewFormRecordBuilder query) {
        try {
            FormCatalog catalog = newCatalog();
            Updater updater = new Updater(catalog);
            updater.create(ResourceId.valueOf(formId), query.toJsonObject());

            return Promise.resolved(null);

        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<FormRecordSet> getRecords(String formId, String parentId) {
        FormCatalog catalog = newCatalog();
        Optional<FormAccessor> collection = catalog.getForm(ResourceId.valueOf(formId));

        JsonArray recordArray = new JsonArray();
        
        if(collection.isPresent()) {
            if(collection.get() instanceof HrdFormAccessor) {
                HrdFormAccessor hrdForm = (HrdFormAccessor) collection.get();
                Iterable<FormRecord> records = hrdForm.getSubmissionsOfParent(ResourceId.valueOf(parentId));
                for (FormRecord record : records) {
                    recordArray.add(record.toJsonElement());
                }
            }
        }
        JsonObject object = new JsonObject();
        object.addProperty("formId", formId);
        object.add("records", recordArray);
        
        return Promise.resolved(FormRecordSet.fromJson(object));
    }

    @Override
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {
        try {
            FormCatalog catalog = newCatalog();
            ColumnSetBuilder builder = new ColumnSetBuilder(catalog);

            return Promise.resolved(builder.build(query));
        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }
}
