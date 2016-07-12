package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.FormNotFoundException;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.hrd.HrdCatalog;
import org.activityinfo.store.mysql.collections.*;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.ActivityLoader;
import org.activityinfo.store.mysql.metadata.DatabaseCache;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MySqlSession implements CollectionCatalog {

    private static Logger LOGGER = Logger.getLogger(MySqlSession.class.getName());
    
    private List<CollectionProvider> providers = new ArrayList<>();
    private final QueryExecutor executor;
    private LoadingCache<ResourceId, Optional<ResourceCollection>> sessionCache;
    private final ActivityLoader activityLoader;

    public MySqlSession(final QueryExecutor executor) {

        activityLoader = new ActivityLoader(executor);
        DatabaseCache databaseCache = new DatabaseCache(executor);

        providers.add(new SimpleTableCollectionProvider(new DatabaseTable(), CollectionPermissions.readonly()));
        providers.add(new SimpleTableCollectionProvider(new UserTable(), CollectionPermissions.readonly()));
        providers.add(new SimpleTableCollectionProvider(new CountryTable(), CollectionPermissions.readonly()));
        providers.add(new SimpleTableCollectionProvider(new AdminEntityTable(), CollectionPermissions.readonly()));
        providers.add(new SimpleTableCollectionProvider(new PartnerTable(databaseCache), CollectionPermissions.readonly()));
        providers.add(new SimpleTableCollectionProvider(new ProjectTable(databaseCache), CollectionPermissions.readonly()));
        providers.add(new TargetCollectionProvider());
        providers.add(new ActivityCollectionProvider(activityLoader));
        providers.add(new LocationCollectionProvider());
        providers.add(new HrdProvider());

        this.executor = executor;
        this.sessionCache = CacheBuilder.newBuilder().build(new CacheLoader<ResourceId, Optional<ResourceCollection>>() {
            @Override
            public Optional<ResourceCollection> load(ResourceId id) throws Exception {
                
                for (CollectionProvider provider : providers) {
                    if (provider.accept(id)) {
                        try {
                            Stopwatch stopwatch = Stopwatch.createStarted();
                            ResourceCollection collection;
                            try {
                                collection = provider.openCollection(executor, id);
                            } catch (FormNotFoundException e) {
                                return Optional.absent();
                            }
                            Optional<ResourceCollection> result = Optional.of(collection);
                            
                            LOGGER.log(Level.INFO, "Opened collection " + id + " in " + stopwatch);
                            
                            return result;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return Optional.absent();
            }
        });
    }

    public ActivityLoader getActivityLoader() {
        return activityLoader;
    }

    @Override
    public Optional<ResourceCollection> getCollection(ResourceId resourceId) {
        try {
            return sessionCache.get(resourceId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public Optional<ResourceCollection> lookupCollection(ResourceId resourceId) {
        for (CollectionProvider mapping : providers) {
            try {
                Optional<ResourceId> collectionId = mapping.lookupCollection(executor, resourceId);
                if (collectionId.isPresent()) {
                    return getCollection(collectionId.get());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.absent();
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> collectionIds) {
        Map<ResourceId, FormClass> resultMap = new HashMap<>();
        Set<ResourceId> toFetch = new HashSet<>();

        // First check sessionCache for any collections which are already loaded
        for (ResourceId collectionId : collectionIds) {
            Optional<ResourceCollection> collection = sessionCache.getIfPresent(collectionId);
            if(collection != null && collection.isPresent()) {
                resultMap.put(collectionId, collection.get().getFormClass());
            } else {
                toFetch.add(collectionId);
            }
        }
        
        // Now consult each of our providers for collections
        try {
            for (CollectionProvider provider : providers) {
                Map<ResourceId, ResourceCollection> fetched = provider.openCollections(executor, toFetch);
                for (Map.Entry<ResourceId, ResourceCollection> entry : fetched.entrySet()) {
                    if(resultMap.containsKey(entry.getKey())) {
                        throw new IllegalStateException("Collection " + entry.getKey() + " returned by multiple providers");
                    }
                    sessionCache.put(entry.getKey(), Optional.of(entry.getValue()));
                    resultMap.put(entry.getKey(), entry.getValue().getFormClass());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        // Finally, verify that all collections were loaded
        for (ResourceId collectionId : collectionIds) {
            if(!resultMap.containsKey(collectionId)) {
                throw new FormNotFoundException(collectionId);
            }
        }
        
        return resultMap;
    }

    @Override
    public FormClass getFormClass(ResourceId formClassId) {
        Optional<ResourceCollection> collection = getCollection(formClassId);
        if(!collection.isPresent()) {
            throw new IllegalStateException("FormClass " + formClassId + " does not exist.");
        }
        return collection.get().getFormClass();
    }


    public void createOrUpdateFormSchema(FormClass formClass) {
        if(formClass.getId().getDomain() == CuidAdapter.ACTIVITY_DOMAIN) {
            // Only update of activity's schemas is currently supported
            Optional<ResourceCollection> collection = getCollection(formClass.getId());
            if(!collection.isPresent()) {
                throw new UnsupportedOperationException("Activity " + formClass.getId() + " does not exist.");
            }
            collection.get().updateFormClass(formClass);
        } else {
            HrdCatalog catalog = new HrdCatalog();
            catalog.create(formClass);
        }
    }
}
