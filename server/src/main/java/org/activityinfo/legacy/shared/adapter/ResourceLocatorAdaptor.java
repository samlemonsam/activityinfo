package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.IdCriteria;
import org.activityinfo.core.shared.criteria.RemoveFieldCriteriaVisitor;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.adapter.bindings.SiteBinding;
import org.activityinfo.legacy.shared.adapter.bindings.SiteBindingFactory;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.UpdateFormClass;
import org.activityinfo.legacy.shared.command.UpdateFormInstance;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservablePromise;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromiseExecutionOperation;
import org.activityinfo.promise.PromisesExecutionGuard;
import org.activityinfo.promise.PromisesExecutionMonitor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Exposes a legacy {@code Dispatcher} implementation as new {@code ResourceLocator}
 */
public class ResourceLocatorAdaptor implements ResourceLocator {

    private final Dispatcher dispatcher;
    private final ClassProvider classProvider;

    public ResourceLocatorAdaptor(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.classProvider = new ClassProvider(dispatcher);
    }

    @Override
    public Promise<FormClass> getFormClass(ResourceId classId) {
        return classProvider.apply(classId);
    }

    @Override
    public Observable<Resource> fetchResource(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<ColumnSet> queryTable(QueryModel queryModel) {
        return new ObservablePromise<>(new HttpQueryExecutor().query(queryModel));
    }

    @Override
    public Promise<FormInstance> getFormInstance(ResourceId instanceId) {
        if(instanceId.getDomain() == CuidAdapter.SITE_DOMAIN) {
            final Promise<SiteResult> site = dispatcher.execute(GetSites.byId(CuidAdapter.getLegacyIdFromCuid(instanceId)));
            final Promise<ActivityFormDTO> form = site.join(new Function<SiteResult, Promise<ActivityFormDTO>>() {
                @Nullable
                @Override
                public Promise<ActivityFormDTO> apply(@Nullable SiteResult input) {
                    if (input != null) {
                        List<SiteDTO> data = input.getData();
                        if (data != null && !data.isEmpty()) {
                            SiteDTO siteDTO = data.get(0);
                            if (siteDTO != null) {
                                return dispatcher.execute(new GetActivityForm(siteDTO.getActivityId()));
                            }
                        }
                    }

                    return Promise.resolved(null);
                }
            });
            return Promise.waitAll(site, form).then(new Function<Void, FormInstance>() {
                @Nullable
                @Override
                public FormInstance apply(@Nullable Void input) {
                    if (form != null) {
                        ActivityFormDTO activityFormDTO = form.get();
                        if (activityFormDTO != null) {
                            SiteBinding binding = new SiteBindingFactory().apply(activityFormDTO);
                            if (site != null) {
                                SiteResult siteResult = site.get();
                                if (siteResult != null) {
                                    List<SiteDTO> data = siteResult.getData();
                                    if (data != null && !data.isEmpty()) {
                                        SiteDTO siteDTO = data.get(0);
                                        if (siteDTO != null) {
                                            return binding.newInstance(siteDTO);
                                        }

                                    }
                                }
                            }
                        }
                    }

                    return null;
                }
            });
        }
        return queryInstances(new IdCriteria(instanceId)).then(new SelectSingle());
    }

    @Override
    public Promise<Void> persist(IsResource resource) {
        if (resource instanceof FormInstance) {
            FormInstance instance = (FormInstance) resource;
            if (instance.getId().getDomain() == CuidAdapter.SITE_DOMAIN) {
                return new SitePersister(dispatcher).persist(instance);

            } else if (instance.getId().getDomain() == CuidAdapter.LOCATION_DOMAIN) {
                return new LocationPersister(dispatcher, instance).persist();
            } else if (instance.getId().getDomain() == ResourceId.GENERATED_ID_DOMAIN) {
                return dispatcher.execute(new UpdateFormInstance(instance)).thenDiscardResult();
            }
        } else if(resource instanceof FormClass) {
            return dispatcher.execute(new UpdateFormClass((FormClass) resource)).thenDiscardResult();
        }
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<Void> persist(List<? extends IsResource> resources) {
        return persist(resources, null);
    }

    @Override
    public Promise<Void> persist(List<? extends IsResource> resources, @Nullable PromisesExecutionMonitor monitor) {

        if (UpdateFormClass.isAllFormClasses(resources)) {
            // goal is to force persistence order and guarantee that subform formclasses are saved before root form class is saved.
            return dispatcher.execute(UpdateFormClass.batchCommandForMultipleFormClasses(resources)).thenDiscardResult();
        }

        final List<PromiseExecutionOperation> operations = Lists.newArrayList();

        for (final IsResource resource : resources) {
            operations.add(new PromiseExecutionOperation() {
                @Override
                public Promise<Void> apply(Void input) {
                    return persist(resource);
                }
            });
        }
        return persistOperation(operations, monitor);
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
        return new QueryExecutor(dispatcher, criteria).execute();
    }

    @Override
    public Promise<List<Projection>> query(final InstanceQuery query) {
        query.setCriteria(query.getCriteria().copy()); // we don't want to modify original criteria to preserve data

        Joiner joiner = new Joiner(dispatcher, query.getFieldPaths(), query.getCriteria());
        if (query.isFilterQuery()) {
            // table filter : fetching unique values for given column
            query.getCriteria().accept(new RemoveFieldCriteriaVisitor());
            return joiner.apply(query).join(new ProjectionsByUniqueColumnFilter(query.getFilterFieldPath()));
        }
        return joiner.apply(query);
    }

    @Override
    public Promise<QueryResult<Projection>> queryProjection(InstanceQuery query) {
        return query(query).then(new InstanceQueryResultAdapter(query));
    }

    public Promise<Void> remove(ResourceId resourceId) {
        return remove(Collections.singleton(resourceId));
    }

    @Override
    public Promise<Void> remove(Collection<ResourceId> resources) {
        return new Eraser(dispatcher, resources).execute();
    }

    @Override
    public Promise<List<FormInstance>> queryInstances(Set<ResourceId> formClassIds) {
        return queryInstances(ClassCriteria.union(formClassIds));
    }
}
