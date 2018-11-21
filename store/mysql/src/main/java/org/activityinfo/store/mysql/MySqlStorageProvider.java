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
package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.HrdStorageProvider;
import org.activityinfo.store.mysql.collections.*;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.ActivityLoader;
import org.activityinfo.store.mysql.metadata.DatabaseCacheImpl;
import org.activityinfo.store.mysql.update.ActivityUpdater;
import org.activityinfo.store.spi.*;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MySqlStorageProvider implements FormStorageProvider, FormCatalog, TransactionalStorageProvider {

    private static final Logger LOGGER = Logger.getLogger(MySqlStorageProvider.class.getName());

    private List<FormProvider> providers = new ArrayList<>();
    private final QueryExecutor executor;
    private LoadingCache<ResourceId, Optional<FormStorage>> sessionCache;
    private final ActivityLoader activityLoader;
    
    private GeodbFolder geodbFolder;
    private DatabasesFolder databasesFolder;
    private final FormFolder formFolder;

    public MySqlStorageProvider(final QueryExecutor executor) {

        activityLoader = new ActivityLoader(executor);
        DatabaseCacheImpl databaseCache = new DatabaseCacheImpl(executor);

        providers.add(new SimpleTableStorageProvider(new UserTable()));
        providers.add(new SimpleTableStorageProvider(new CountryTable()));
        providers.add(new SimpleTableStorageProvider(new AdminEntityTable()));
        providers.add(new SimpleTableStorageProvider(new PartnerTable(databaseCache)));
        providers.add(new SimpleTableStorageProvider(new ProjectTable(databaseCache)));
        providers.add(new TargetFormProvider());
        providers.add(new ActivityFormProvider(activityLoader));
        providers.add(new LocationFormProvider());
        providers.add(new HrdProvider());

        geodbFolder = new GeodbFolder(executor);
        databasesFolder = new DatabasesFolder(executor, this);
        formFolder = new FormFolder(this);
        
        this.executor = executor;
        this.sessionCache = CacheBuilder.newBuilder().build(new CacheLoader<ResourceId, Optional<FormStorage>>() {
            @Override
            public Optional<FormStorage> load(ResourceId id) throws Exception {
                
                for (FormProvider provider : providers) {
                    if (provider.accept(id)) {
                        try {
                            Stopwatch stopwatch = Stopwatch.createStarted();
                            FormStorage storage;
                            try {
                                storage = provider.openForm(executor, id);
                            } catch (FormNotFoundException e) {
                                return Optional.absent();
                            }
                            Optional<FormStorage> result = Optional.of(storage);

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
            // Start async queries
            Iterable<CatalogEntry> analyses = ReportFolder.queryReports(parentId);

            List<CatalogEntry> entries = new ArrayList<>();
            entries.addAll(geodbFolder.getChildren(parentId));
            entries.addAll(databasesFolder.getChildren(parentId, userId));
            entries.addAll(formFolder.getChildren(ResourceId.valueOf(parentId)));
            Iterables.addAll(entries, analyses);
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
            HrdStorageProvider catalog = new HrdStorageProvider();
            catalog.create(formClass);
        }
    }

    private void createFormSchema(FormClass formClass) {
        int activityId = CuidAdapter.getLegacyIdFromCuid(formClass.getId());
        int databaseId = CuidAdapter.getLegacyIdFromCuid(formClass.getDatabaseId());
        begin();
        try {
            ActivityUpdater updater = new ActivityUpdater(activityId, databaseId, executor);
            updater.insert(formClass);
            commit();
        } catch (Exception e) {
            rollback();
            throw e;
        }

    }

    @Override
    public void begin() {
        executor.begin();
    }

    @Override
    public void commit() {
        executor.commit();
    }

    @Override
    public void rollback() {
        executor.rollback();
    }
}
