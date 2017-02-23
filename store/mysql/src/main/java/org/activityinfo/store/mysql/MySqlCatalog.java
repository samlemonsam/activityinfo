package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.HrdCatalog;
import org.activityinfo.store.mysql.collections.*;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.ActivityLoader;
import org.activityinfo.store.mysql.metadata.DatabaseCache;
import org.activityinfo.store.mysql.update.ActivityUpdater;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormNotFoundException;
import org.activityinfo.store.spi.FormPermissions;
import org.activityinfo.store.spi.FormStorage;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MySqlCatalog implements FormCatalog {

    private static Logger LOGGER = Logger.getLogger(MySqlCatalog.class.getName());
    
    private List<FormProvider> providers = new ArrayList<>();
    private final QueryExecutor executor;
    private LoadingCache<ResourceId, Optional<FormStorage>> sessionCache;
    private final ActivityLoader activityLoader;
    
    private GeodbFolder geodbFolder;
    private DatabasesFolder databasesFolder;

    public MySqlCatalog(final QueryExecutor executor) {

        activityLoader = new ActivityLoader(executor);
        DatabaseCache databaseCache = new DatabaseCache(executor);

        providers.add(new SimpleTableStorageProvider(new UserTable(), FormPermissions.readonly()));
        providers.add(new SimpleTableStorageProvider(new CountryTable(), FormPermissions.readonly()));
        providers.add(new SimpleTableStorageProvider(new AdminEntityTable(), new AdminAuthorizer()));
        providers.add(new SimpleTableStorageProvider(new PartnerTable(databaseCache), FormPermissions.readonly()));
        providers.add(new SimpleTableStorageProvider(new ProjectTable(databaseCache), FormPermissions.readonly()));
        providers.add(new TargetFormProvider());
        providers.add(new ActivityFormProvider(activityLoader));
        providers.add(new LocationFormProvider(activityLoader.getPermissionCache()));
        providers.add(new HrdProvider());

        geodbFolder = new GeodbFolder(executor);
        databasesFolder = new DatabasesFolder(executor);
        
        this.executor = executor;
        this.sessionCache = CacheBuilder.newBuilder().build(new CacheLoader<ResourceId, Optional<FormStorage>>() {
            @Override
            public Optional<FormStorage> load(ResourceId id) throws Exception {
                
                for (FormProvider provider : providers) {
                    if (provider.accept(id)) {
                        try {
                            Stopwatch stopwatch = Stopwatch.createStarted();
                            FormStorage collection;
                            try {
                                collection = provider.openForm(executor, id);
                            } catch (FormNotFoundException e) {
                                return Optional.absent();
                            }
                            Optional<FormStorage> result = Optional.of(collection);
                            
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
    public Optional<FormStorage> getForm(ResourceId formId) {
        try {
            return sessionCache.get(formId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public Optional<FormStorage> lookupForm(ResourceId recordId) {
        for (FormProvider mapping : providers) {
            try {
                Optional<ResourceId> collectionId = mapping.lookupForm(executor, recordId);
                if (collectionId.isPresent()) {
                    return getForm(collectionId.get());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.absent();
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        Map<ResourceId, FormClass> resultMap = new HashMap<>();
        Set<ResourceId> toFetch = new HashSet<>();

        // First check sessionCache for any collections which are already loaded
        for (ResourceId collectionId : formIds) {
            Optional<FormStorage> collection = sessionCache.getIfPresent(collectionId);
            if(collection != null && collection.isPresent()) {
                resultMap.put(collectionId, collection.get().getFormClass());
            } else {
                toFetch.add(collectionId);
            }
        }
        
        // Now consult each of our providers for collections
        try {
            for (FormProvider provider : providers) {
                Map<ResourceId, FormStorage> fetched = provider.openForms(executor, toFetch);
                for (Map.Entry<ResourceId, FormStorage> entry : fetched.entrySet()) {
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
        for (ResourceId collectionId : formIds) {
            if(!resultMap.containsKey(collectionId)) {
                throw new FormNotFoundException(collectionId);
            }
        }
        
        return resultMap;
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        
        List<CatalogEntry> entries = new ArrayList<>();
        entries.add(geodbFolder.getRootEntry());
        entries.add(databasesFolder.getRootEntry());

        return entries;
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        
        try {
            List<CatalogEntry> entries = new ArrayList<>();
            entries.addAll(geodbFolder.getChildren(parentId));
            entries.addAll(databasesFolder.getChildren(parentId, userId));

            return entries;
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FormClass getFormClass(ResourceId formClassId) {
        Optional<FormStorage> collection = getForm(formClassId);
        if(!collection.isPresent()) {
            throw new IllegalStateException("FormClass " + formClassId + " does not exist.");
        }
        return collection.get().getFormClass();
    }
    
    QueryExecutor getExecutor() {
        return executor;
    }


    public void createOrUpdateFormSchema(FormClass formClass) {
        if(formClass.getId().getDomain() == CuidAdapter.ACTIVITY_DOMAIN) {
            // Only update of activity's schemas is currently supported
            Optional<FormStorage> collection = getForm(formClass.getId());
            if(collection.isPresent()) {
                collection.get().updateFormClass(formClass);
            } else {
                createFormSchema(formClass);
            }
        } else {
            HrdCatalog catalog = new HrdCatalog();
            catalog.create(formClass);
        }
    }

    private void createFormSchema(FormClass formClass) {
        int activityId = CuidAdapter.getLegacyIdFromCuid(formClass.getId());
        int databaseId = CuidAdapter.getLegacyIdFromCuid(formClass.getDatabaseId());
        ActivityUpdater updater = new ActivityUpdater(activityId, databaseId, executor);
        updater.insert(formClass);

    }
}
