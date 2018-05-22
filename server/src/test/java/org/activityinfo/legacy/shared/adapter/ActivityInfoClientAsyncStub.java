/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.FormRecordUpdateBuilder;
import org.activityinfo.api.client.NewFormRecordBuilder;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.analysis.Analysis;
import org.activityinfo.model.analysis.AnalysisUpdate;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.authentication.AuthenticationModuleStub;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.store.hrd.HrdFormStorage;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.BlobAuthorizer;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.FormStorageProvider;

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

    private FormStorageProvider newCatalog() {

        // Reset the current transaction
        if(entityManager.get().getTransaction().isActive()) {
            entityManager.get().getTransaction().commit();
        }

        // Create a fresh catalog to simulate a new request
        return new MySqlStorageProvider(new HibernateQueryExecutor(entityManager));
    }

    @Override
    public Promise<FormClass> getFormSchema(String formId) {
        FormStorageProvider catalog = newCatalog();

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
        FormStorageProvider newCatalog = newCatalog();
        FormTreeBuilder treeBuilder = new FormTreeBuilder(newCatalog);
        return Promise.resolved(treeBuilder.queryTree(formId));
    }


    @Override
    public Promise<Void> updateFormSchema(String formId, FormClass updatedSchema) {
        FormStorageProvider catalog = newCatalog();

        try {
            EntityTransaction tx = entityManager.get().getTransaction();
            tx.begin();

            Optional<FormStorage> collection = catalog.getForm(updatedSchema.getId());
            if(collection.isPresent()) {
                collection.get().updateFormClass(updatedSchema);
            } else {
                ((MySqlStorageProvider) catalog).createOrUpdateFormSchema(updatedSchema);
            }

            tx.commit();

            return Promise.resolved(null);

        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    private FormClass defensiveCopy(FormClass formClass) {
        return FormClass.fromJson(formClass.toJson());
    }


    @Override
    public Promise<UserDatabaseMeta> getDatabase(ResourceId databaseId) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<List<CatalogEntry>> getFormCatalog(String parent) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<Maybe<FormRecord>> getRecord(String formId, String recordId) {
        try {
            FormStorageProvider catalog = newCatalog();
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
            FormStorageProvider catalog = newCatalog();
            Updater updater = new Updater(catalog, currentUserId(), blobAuthorizer, new SerialNumberProviderStub());
            updater.execute(ResourceId.valueOf(formId), ResourceId.valueOf(recordId), query.toJsonObject());

            return Promise.resolved(null);
            
        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<RecordHistory> getRecordHistory(String formId, String recordId) {
        return Promise.resolved(RecordHistory.create(Collections.<RecordHistoryEntry>emptyList()));
    }

    private int currentUserId() {
        return AuthenticationModuleStub.getCurrentUser().getUserId();
    }

    @Override
    public Promise<Void> createRecord(String formId, NewFormRecordBuilder query) {
        try {
            FormStorageProvider catalog = newCatalog();
            Updater updater = new Updater(catalog, currentUserId(), blobAuthorizer,  new SerialNumberProviderStub());
            updater.create(ResourceId.valueOf(formId), query.toJsonObject());

            return Promise.resolved(null);

        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<FormRecordSet> getRecords(String formId, String parentId) {
        FormStorageProvider catalog = newCatalog();
        Optional<FormStorage> collection = catalog.getForm(ResourceId.valueOf(formId));

        JsonValue recordArray = Json.createArray();
        
        if(collection.isPresent()) {
            if(collection.get() instanceof HrdFormStorage) {
                HrdFormStorage hrdForm = (HrdFormStorage) collection.get();
                Iterable<FormRecord> records = hrdForm.getSubRecords(ResourceId.valueOf(parentId));
                for (FormRecord record : records) {
                    recordArray.add(record.toJson());
                }
            }
        }
        JsonValue object = createObject();
        object.put("formId", formId);
        object.put("records", recordArray);
        
        return Promise.resolved(FormRecordSet.fromJson(object));
    }

    @Override
    public Promise<FormSyncSet> getRecordVersionRange(String formId, long localVersion, long toVersion, java.util.Optional<String> cursor) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {
        try {
            FormStorageProvider catalog = newCatalog();
            ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormSupervisor());

            return Promise.resolved(builder.build(query));
        } catch (Exception e) {
            return Promise.rejected(e);
        }
    }

    @Override
    public Promise<Void> updateRecords(RecordTransaction transactions) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
    }

    @Override
    public Promise<Maybe<Analysis>> getAnalysis(String id) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<Void> updateAnalysis(AnalysisUpdate analysis) {
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
