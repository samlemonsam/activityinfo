package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Functions;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.InstanceQueryResult;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Cuid;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.Resource;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.IdCriteria;
import org.activityinfo.core.shared.form.FormClass;
import org.activityinfo.core.shared.form.FormInstance;
import org.activityinfo.fp.client.Promise;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.adapter.bindings.SiteBinding;
import org.activityinfo.legacy.shared.adapter.bindings.SiteBindingFactory;
import org.activityinfo.legacy.shared.command.GetSchema;

import java.util.Collection;
import java.util.List;

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
    public Promise<FormClass> getFormClass(Cuid classId) {
        return classProvider.apply(classId);
    }

    @Override
    public Promise<FormInstance> getFormInstance(Cuid instanceId) {
        return queryInstances(new IdCriteria(instanceId))
               .then(new SelectSingle());
    }

    @Override
    public Promise<Void> persist(Resource resource) {
        if (resource instanceof FormInstance) {
            FormInstance instance = (FormInstance) resource;
            if (instance.getId().getDomain() == CuidAdapter.SITE_DOMAIN) {
                int activityId = CuidAdapter.getLegacyIdFromCuid(instance.getClassId());

                Promise<SiteBinding> siteBinding = dispatcher
                        .execute(new GetSchema())
                        .then(new SiteBindingFactory(activityId));

                return Promise.fmap(new SitePersistFunction(dispatcher))
                        .apply(siteBinding, Promise.resolved(instance))
                        .then(Functions.<Void>constant(null));

            } else if (instance.getId().getDomain() == CuidAdapter.LOCATION_DOMAIN) {
                return new LocationPersister(dispatcher, instance).persist();
            }
        } else if (resource instanceof FormClass) {
            FormClass formClass = (FormClass) resource;
            // todo
        }
        return Promise.rejected(new UnsupportedOperationException());
    }

    // todo there should be better way to persist list of resources
    @Override
    public Promise<Void> persist(List<? extends Resource> resources) {
        if (resources != null && !resources.isEmpty()) {
            for (Resource resource : resources) {
                final Promise<Void> intermediatePromise = persist(resource);
                if (intermediatePromise.getState() != Promise.State.FULFILLED) {
                    return intermediatePromise;
                }
            }
        }
        return Promise.resolved(null);
    }

    @Override
    public Promise<Integer> countInstances(Criteria criteria) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<List<FormInstance>> queryInstances(Criteria criteria) {
        return new QueryExecutor(dispatcher, criteria).execute();
    }

    @Override
    public Promise<List<Projection>> query(InstanceQuery query) {
        return new Joiner(dispatcher, query.getFieldPaths(), query.getCriteria()).apply(query);
    }

    public Promise<InstanceQueryResult> queryProjection(InstanceQuery query) {
        return query(query).then(new InstanceQueryResultAdapter());
    }


    @Override
    public Promise<Void> remove(Collection<Cuid> resources) {
        return new Eraser(dispatcher, resources).execute();
    }
}
