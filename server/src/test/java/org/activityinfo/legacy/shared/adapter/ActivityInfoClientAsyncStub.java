package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.activityinfo.api.client.*;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.authentication.AuthenticationModuleStub;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.store.hrd.HrdFormStorage;
import org.activityinfo.store.mysql.MySqlCatalog;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.BlobAuthorizer;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Collections;
import java.util.List;

import static org.activityinfo.json.Json.createObject;

public class ActivityInfoClientAsyncStub implements ActivityInfoClientAsync {

    private Provider<EntityManager> entityManager;
    private BlobAuthorizer blobAuthorizer;

    @Inject
    public ActivityInfoClientAsyncStub(Provider<EntityManager> entityManager,
                                       BlobAuthorizer blobAuthorizer) {
        this.entityManager = entityManager;
        this.blobAuthorizer = blobAuthorizer;
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
    public Promise<FormMetadata> getFormMetadata(String formId) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<FormTree> getFormTree(ResourceId formId) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Promise<Void> updateFormSchema(String formId, FormClass updatedSchema) {
        FormCatalog catalog = newCatalog();

        try {
            EntityTransaction tx = entityManager.get().getTransaction();
            tx.begin();

            Optional<FormStorage> collection = catalog.getForm(updatedSchema.getId());
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
        return FormClass.fromJson(formClass.toJsonObject());
    }


    @Override
    public Promise<List<CatalogEntry>> getFormCatalog(String parent) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<Maybe<FormRecord>> getRecord(String formId, String recordId) {
        try {
            FormCatalog catalog = newCatalog();
            Optional<FormStorage> storage = catalog.getForm(ResourceId.valueOf(formId));
            if(!storage.isPresent()) {
                return Promise.resolved(Maybe.<FormRecord>notFound());
            }
            Optional<FormRecord> record = storage.get().get(ResourceId.valueOf(recordId));
            if(!record.isPresent()) {
                return Promise.resolved(Maybe.<FormRecord>notFound());
            }
            return Promise.resolved(Maybe.of(record.get()));

        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<Void> updateRecord(String formId, String recordId, FormRecordUpdateBuilder query) {
        try {
            FormCatalog catalog = newCatalog();
            Updater updater = new Updater(catalog, currentUserId(), blobAuthorizer, new SerialNumberProviderStub());
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

    private int currentUserId() {
        return AuthenticationModuleStub.getCurrentUser().getUserId();
    }

    @Override
    public Promise<Void> createRecord(String formId, NewFormRecordBuilder query) {
        try {
            FormCatalog catalog = newCatalog();
            Updater updater = new Updater(catalog, currentUserId(), blobAuthorizer,  new SerialNumberProviderStub());
            updater.create(ResourceId.valueOf(formId), query.toJsonObject());

            return Promise.resolved(null);

        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<FormRecordSet> getRecords(String formId, String parentId) {
        FormCatalog catalog = newCatalog();
        Optional<FormStorage> collection = catalog.getForm(ResourceId.valueOf(formId));

        org.activityinfo.json.JsonArray recordArray = Json.createArray();
        
        if(collection.isPresent()) {
            if(collection.get() instanceof HrdFormStorage) {
                HrdFormStorage hrdForm = (HrdFormStorage) collection.get();
                Iterable<FormRecord> records = hrdForm.getSubRecords(ResourceId.valueOf(parentId));
                for (FormRecord record : records) {
                    recordArray.add(record.toJsonElement());
                }
            }
        }
        JsonObject object = createObject();
        object.put("formId", formId);
        object.put("records", recordArray);
        
        return Promise.resolved(FormRecordSet.fromJson(object));
    }

    @Override
    public Promise<FormRecordSet> getRecordVersionRange(String formId, long localVersion, long toVersion) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {
        try {
            FormCatalog catalog = newCatalog();
            ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormSupervisor());

            return Promise.resolved(builder.build(query));
        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<Void> updateRecords(RecordTransactionBuilder transactions) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public <T extends JobDescriptor<R>, R extends JobResult> Promise<JobStatus<T, R>> startJob(T job) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<JobStatus<?, ?>> getJobStatus(String jobId) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }
}
