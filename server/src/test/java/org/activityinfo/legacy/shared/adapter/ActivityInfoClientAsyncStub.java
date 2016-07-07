package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.FormRecordUpdateBuilder;
import org.activityinfo.api.client.NewFormRecordBuilder;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.MySqlSession;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.Updater;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class ActivityInfoClientAsyncStub implements ActivityInfoClientAsync {

    private Provider<EntityManager> entityManager;

    @Inject
    public ActivityInfoClientAsyncStub(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    private CollectionCatalog newCatalog() {

        // Reset the current transaction
        if(entityManager.get().getTransaction().isActive()) {
            entityManager.get().getTransaction().commit();
        }

        // Create a fresh catalog to simulate a new request
        return new MySqlSession(new HibernateQueryExecutor(entityManager));
    }

    @Override
    public Promise<FormClass> getFormSchema(String formId) {
        CollectionCatalog catalog = newCatalog();

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
        CollectionCatalog catalog = newCatalog();

        try {
            EntityTransaction tx = entityManager.get().getTransaction();
            tx.begin();

            Optional<ResourceCollection> collection = catalog.getCollection(updatedSchema.getId());
            if(!collection.isPresent()) {
                throw new RuntimeException("No such form " + updatedSchema.getId());
            }
            collection.get().updateFormClass(updatedSchema);

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
    public Promise<FormRecord> getRecord(String formId, String recordId) {
        try {
            CollectionCatalog catalog = newCatalog();
            Optional<ResourceCollection> collection = catalog.getCollection(ResourceId.valueOf(formId));
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
    public Promise<FormRecord> updateRecord(String formId, String recordId, FormRecordUpdateBuilder query) {
        try {
            CollectionCatalog catalog = newCatalog();
            Updater updater = new Updater(catalog);
            updater.execute(ResourceId.valueOf(formId), ResourceId.valueOf(recordId), query.toJsonObject());

            return Promise.resolved(null);
            
        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<Void> createRecord(String formId, NewFormRecordBuilder query) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {
        try {
            CollectionCatalog catalog = newCatalog();
            ColumnSetBuilder builder = new ColumnSetBuilder(catalog);

            return Promise.resolved(builder.build(query));
        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }
}
