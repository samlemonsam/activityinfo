package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.collections.CollectionProvider;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


class MySqlSession implements CollectionCatalog {

    private static Logger LOGGER = Logger.getLogger(MySqlSession.class.getName());
    
    private List<CollectionProvider> providers;
    private final QueryExecutor executor;
    private LoadingCache<ResourceId, Optional<ResourceCollection>> sessionCache;

    public MySqlSession(final List<CollectionProvider> providers, final QueryExecutor executor) {
        this.providers = providers;
        this.executor = executor;
        this.sessionCache = CacheBuilder.newBuilder().build(new CacheLoader<ResourceId, Optional<ResourceCollection>>() {
            @Override
            public Optional<ResourceCollection> load(ResourceId id) throws Exception {
                
                for (CollectionProvider provider : providers) {
                    if (provider.accept(id)) {
                        try {
                            Stopwatch stopwatch = Stopwatch.createStarted();
                            Optional<ResourceCollection> result = Optional.of(provider.openCollection(executor, id));

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
    public FormClass getFormClass(ResourceId formClassId) {
        return getCollection(formClassId).get().getFormClass();
    }
}
