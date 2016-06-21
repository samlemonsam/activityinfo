package org.activityinfo.legacy.shared.adapter;

import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryModelAdapter;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromiseExecutionOperation;
import org.activityinfo.promise.PromisesExecutionMonitor;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.CompositeCatalog;
import org.activityinfo.store.hrd.HrdCatalog;
import org.activityinfo.store.mysql.MySqlSession;
import org.activityinfo.store.query.impl.ColumnSetBuilder;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This is a {@code ResourceLocator} implementation that is meant to keep existing tests running
 * against MySqlCatalog.
 */
public class TestingResourceLocatorAdapter implements ResourceLocator {
    
    private Provider<EntityManager> entityManager;

    @Inject
    public TestingResourceLocatorAdapter(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    private CollectionCatalog newCatalog() {
        
        // Reset the current transaction
        if(entityManager.get().getTransaction().isActive()) {
            entityManager.get().getTransaction().commit();
        }
        
        // Create a fresh catalog to simulate a new request
        CompositeCatalog catalog = new CompositeCatalog(
                new HrdCatalog(), 
                new MySqlSession(new HibernateQueryExecutor(entityManager)));
        
        return catalog;
    }
    
    @Override
    public Promise<FormClass> getFormClass(ResourceId formId) {

        CollectionCatalog catalog = newCatalog();

        FormClass formClass;
        try {
            formClass = defensiveCopy(catalog.getFormClass(formId));
        } catch (Exception e) {
            return Promise.rejected(e);
        }
        return Promise.resolved(formClass);
    }

    private FormClass defensiveCopy(FormClass formClass) {
        return FormClass.fromResource(formClass.asResource());
    }

    @Override
    public Observable<Resource> fetchResource(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Observable<ColumnSet> queryTable(QueryModel queryModel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<FormInstance> getFormInstance(ResourceId formId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<Void> persist(IsResource resource) {
        CollectionCatalog catalog = newCatalog();


        if(resource instanceof FormClass) {
            try {

                EntityTransaction tx = entityManager.get().getTransaction();
                tx.begin();
                
                FormClass formClass = (FormClass) resource;
                catalog.getCollection(formClass.getId()).get().updateFormClass(formClass);

                tx.commit();
                
            } catch (Exception e) {
                return Promise.rejected(e);
            }
            return Promise.resolved(null);
        } else {
            return Promise.rejected(new UnsupportedOperationException());
        }
    }

    @Override
    public Promise<Void> persist(List<? extends IsResource> resources) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<Void> persist(List<? extends IsResource> resources, @Nullable PromisesExecutionMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<Void> persistOperation(List<PromiseExecutionOperation> operations) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Promise<Void> persistOperation(List<PromiseExecutionOperation> resources, @Nullable PromisesExecutionMonitor monitor) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Promise<QueryResult<FormInstance>> queryInstances(InstanceQuery criteria) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Promise<List<FormInstance>> queryInstances(Criteria criteria) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Promise<QueryResult<Projection>> queryProjection(InstanceQuery query) {
        QueryModelAdapter adapter = new QueryModelAdapter();
        QueryModel queryModel = adapter.build(query);

        CollectionCatalog catalog = newCatalog();

        ColumnSetBuilder columnSetBuilder = new ColumnSetBuilder(catalog);
        ColumnSet columnSet = columnSetBuilder.build(queryModel);

        return Promise.resolved(adapter.toQueryResult(columnSet));

    }

    @Override
    public Promise<List<Projection>> query(InstanceQuery query) {

        QueryModelAdapter adapter = new QueryModelAdapter();
        QueryModel queryModel = adapter.build(query);
        
        CollectionCatalog catalog = newCatalog();
        
        ColumnSetBuilder columnSetBuilder = new ColumnSetBuilder(catalog);
        ColumnSet columnSet = columnSetBuilder.build(queryModel);
        
        return Promise.resolved(adapter.toProjections(columnSet));

    }

    @Override
    public Promise<Void> remove(ResourceId resourceId) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Promise<Void> remove(Collection<ResourceId> resources) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Promise<List<FormInstance>> queryInstances(Set<ResourceId> resourceIds) {
        throw new UnsupportedOperationException();

    }
}
