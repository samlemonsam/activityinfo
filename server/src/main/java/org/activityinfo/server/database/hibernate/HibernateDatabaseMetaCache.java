package org.activityinfo.server.database.hibernate;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.json.Json;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.database.RecordLock;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.ResourceType;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.store.spi.DatabaseMetaCache;
import org.activityinfo.store.spi.FormStorageProvider;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Caching mechanism for DatabaseMeta. The cache has three components:
 * <ol>
 *     <li>Request-level in-memory cache for DatabaseMeta, using Guava LoadingCache as backing cache.</li>
 *     <li>Distributed in-memory cache, using Appengine Memcache.</li>
 *     <li>Database Loader, using Hibernate EntityManager.</li>
 * </ol>
 *     The cache will attempt to retrieve DatabaseMeta from the request cache first, then Memcache, and if that
 *     fails then loads and builds the DatabaseMeta from the MySQL Database. DatabaseMeta, when constructed, will be
 *     stored in Memcache and request cache.
 * </p>
 *
 * <p>DatabaseMeta are keyed in the request cache by the ResourceId of the Database.</p>
 */
public class HibernateDatabaseMetaCache implements DatabaseMetaCache {

    private static final Logger LOGGER = Logger.getLogger(HibernateDatabaseMetaCache.class.getName());

    private static final String CACHE_PREFIX = "dbMeta";
    private static final String CACHE_VERSION = "6";

    private static final long MAX_CACHE_SIZE = 50;
    private static final long EXPIRES_IN = 10;

    private final Provider<EntityManager> entityManager;
    private final FormStorageProvider formStorageProvider;
    private final MemcacheService memcacheService;
    private final BillingAccountOracle billingAccountOracle;

    private final LoadingCache<ResourceId,Optional<DatabaseMeta>> cache;

    @Inject
    public HibernateDatabaseMetaCache(Provider<EntityManager> entityManager,
                                      FormStorageProvider formStorageProvider,
                                      MemcacheService memcacheService,
                                      BillingAccountOracle billingAccountOracle) {
        this.entityManager = entityManager;
        this.formStorageProvider = formStorageProvider;
        this.memcacheService = memcacheService;
        this.billingAccountOracle = billingAccountOracle;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterAccess(EXPIRES_IN, TimeUnit.MINUTES)
                .build(buildCacheLoader());
    }

    @Override
    public Optional<DatabaseMeta> load(ResourceId toLoad) {
        try {
            return cache.get(toLoad);
        } catch (Exception loadFailure) {
            throw new RuntimeException(loadFailure);
        }
    }

    @Override
    public Map<ResourceId, DatabaseMeta> loadAll(Set<ResourceId> toLoad) {
        try {
            return cache.getAll(toLoad).values().stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toMap(DatabaseMeta::getDatabaseId, db -> db));
        } catch (Exception loadFailure) {
            throw new RuntimeException(loadFailure);
        }
    }

    private CacheLoader<ResourceId,Optional<DatabaseMeta>> buildCacheLoader() {
        return new CacheLoader<ResourceId,Optional<DatabaseMeta>>() {
            @Override
            public Optional<DatabaseMeta> load(ResourceId key) throws Exception {
                // Load version
                Map<ResourceId,Long> toLoad = queryDatabaseVersions(Collections.singleton(key));
                if (toLoad.isEmpty()) {
                    return Optional.empty();
                }

                // Load from Memcache
                Map<ResourceId,Optional<DatabaseMeta>> loadedFromMemcache = loadFromMemcache(toLoad);
                if (!loadedFromMemcache.isEmpty()) {
                    return loadedFromMemcache.get(key);
                }

                // Load from Database
                Map<ResourceId,DatabaseMeta> loadedFromDb = loadFromDb(toLoad.keySet());
                if (!loadedFromDb.isEmpty()) {
                    cacheToMemcache(loadedFromDb.values());
                    return Optional.of(loadedFromDb.get(key));
                }

                return Optional.empty();
            }

            @Override
            public Map<ResourceId,Optional<DatabaseMeta>> loadAll(Iterable<? extends ResourceId> keys) throws Exception {
                Set<ResourceId> keySet = Sets.newHashSet(keys);

                // Load versions
                Map<ResourceId,Long> toLoad = queryDatabaseVersions(keySet);
                if (toLoad.isEmpty()) {
                    return mapMissingEntries(keySet);
                }
                Map<ResourceId,Optional<DatabaseMeta>> loaded = new HashMap<>(toLoad.size());

                // Create set of missing entries which cannot be loaded
                Set<ResourceId> missing = Sets.difference(keySet,toLoad.keySet()).immutableCopy();

                // Load from Memcache
                loaded.putAll(loadFromMemcache(toLoad));
                loaded.keySet().forEach(toLoad::remove);
                if (toLoad.isEmpty()) {
                    loaded.putAll(mapMissingEntries(missing));
                    return loaded;
                }

                // Load from Database
                Map<ResourceId,DatabaseMeta> loadedFromDb = loadFromDb(toLoad.keySet());
                if (!loadedFromDb.isEmpty()) {
                    cacheToMemcache(loadedFromDb.values());
                    loaded.putAll(loadedFromDb.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    db -> Optional.of(db.getValue()))));
                    loadedFromDb.keySet().forEach(toLoad::remove);
                }

                // Return Optional.empty for database(s) left in "toLoad" and for missing databases
                loaded.putAll(mapMissingEntries(toLoad.keySet()));
                loaded.putAll(mapMissingEntries(missing));

                return loaded;
            }
        };
    }

    private static Map<ResourceId,Optional<DatabaseMeta>> mapMissingEntries(Set<ResourceId> keySet) {
        return keySet.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> Optional.empty()));
    }

    private Map<ResourceId,Long> queryDatabaseVersions(@NotNull Set<ResourceId> databaseIds) {
        return entityManager.get().createQuery("SELECT db.id, db.metaVersion " +
                "FROM Database db " +
                "WHERE db.id in :dbIds", Object[].class)
                .setParameter("dbIds", databaseIds.stream().map(CuidAdapter::getLegacyIdFromCuid).collect(Collectors.toSet()))
                .getResultList().stream()
                .collect(Collectors.toMap(
                        result -> CuidAdapter.databaseId(((int) result[0])),
                        result -> (long) result[1]));
    }

    private Map<ResourceId,Optional<DatabaseMeta>> loadFromMemcache(Map<ResourceId,Long> toFetch) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseMeta from Memcache", toFetch.size()));
        Map<ResourceId,Optional<DatabaseMeta>> loaded = new HashMap<>(toFetch.size());
        Map<String,ResourceId> fetchKeys = toFetch.entrySet().stream()
                .collect(Collectors.toMap(
                        fetch -> memcacheKey(fetch.getKey(),fetch.getValue()),
                        Map.Entry::getKey));
        try {
            Map<String,Object> cached = memcacheService.getAll(fetchKeys.keySet());
            loaded.putAll(cached.entrySet().stream()
                    .collect(Collectors.toMap(
                            cachedDb -> fetchKeys.get(cachedDb.getKey()),
                            cachedDb -> Optional.of(deserialize((String) cachedDb.getValue()))
                    )));
            LOGGER.info(() -> String.format("Fetched %d/%d DatabaseMeta from Memcache", loaded.size(), toFetch.size()));
        } catch (Exception ignorable) {
            // Memcache load failed, but we can still retrieve from database
            LOGGER.severe(String.format("Fetching failed for %d DatabaseMeta from Memcache", toFetch.size()));
        }
        return loaded;
    }

    private DatabaseMeta deserialize(String serializedDatabaseMeta) {
        return DatabaseMeta.fromJson(Json.parse(serializedDatabaseMeta));
    }

    private void cacheToMemcache(Collection<DatabaseMeta> loadedDbs) {
        Map<String,String> toCache = loadedDbs.stream()
                .collect(Collectors.toMap(
                        db -> memcacheKey(db.getDatabaseId(),db.getVersion()),
                        db -> db.toJson().toJson()));
        LOGGER.info(() -> String.format("Caching %d DatabaseMeta to Memcache", toCache.size()));
        try {
            memcacheService.putAll(toCache);
        } catch (Exception ignorable) {
            // Caching failed, but is not terminal
            LOGGER.severe(String.format("Caching failed for %d DatabaseMeta to Memcache", loadedDbs.size()));
        }
    }

    public static String memcacheKey(ResourceId databaseId, long version) {
        return String.format("%s:%s_%s:%d", CACHE_PREFIX, CACHE_VERSION,  databaseId.asString(), version);
    }

    private Map<ResourceId,DatabaseMeta> loadFromDb(Set<ResourceId> toFetch) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseMeta from MySqlDatabase", toFetch.size()));
        Map<Integer,ResourceId> fetchKeys = toFetch.stream().collect(Collectors.toMap(CuidAdapter::getLegacyIdFromCuid, id -> id));
        Set<Integer> suspendedDatabases = billingAccountOracle.getSuspendedDatabases(fetchKeys.keySet());
        return entityManager.get().createQuery("SELECT db " +
                "FROM Database db " +
                "WHERE db.id IN :databaseIds", Database.class)
                .setParameter("databaseIds", fetchKeys.keySet())
                .getResultList().stream()
                .map(db -> buildMeta(db, suspendedDatabases.contains(db.getId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DatabaseMeta::getDatabaseId, db -> db));
    }

    private @Nullable DatabaseMeta buildMeta(@Nullable Database database, boolean suspended) {
        if (database == null) {
            return null;
        }
        if (database.isDeleted()) {
            return buildDeletedMeta(database);
        }
        return new DatabaseMeta.Builder()
                .setDatabaseId(CuidAdapter.databaseId(database.getId()))
                .setOwnerId(database.getOwner().getId())
                .setVersion(database.getMetaVersion())
                .setLabel(database.getName())
                .setDescription(database.getFullName())
                .setPublished(false)
                .setPendingTransfer(database.hasPendingTransfer())
                .addResources(fetchResources(database))
                .addLocks(fetchLocks(database))
                .setSuspended(suspended)
                .build();
    }

    private @Nullable DatabaseMeta buildDeletedMeta(@NotNull Database database) {
        return new DatabaseMeta.Builder()
                .setDatabaseId(CuidAdapter.databaseId(database.getId()))
                .setOwnerId(database.getOwner().getId())
                .setVersion(database.getMetaVersion())
                .setDeleted(true)
                .build();
    }

    private List<Resource> fetchResources(@NotNull Database database) {
        List<Resource> resources = new ArrayList<>();

        List<Resource> formResources = fetchForms(database);
        List<Resource> monthlyReportingResources = fetchMonthlyReportingSubForms(database);
        List<Resource> subFormResources = fetchSubForms(formResources);
        List<Resource> folderResources = fetchFolders(database);
        List<Resource> locationTypeResources = fetchLocationTypes(database);

        Resource partnerResource = partnerFormResource(database);

        resources.addAll(formResources);
        resources.addAll(monthlyReportingResources);
        resources.addAll(subFormResources);
        resources.addAll(folderResources);
        resources.add(partnerResource);
        resources.addAll(locationTypeResources);

        return resources;
    }

    private Resource partnerFormResource(Database database) {
        return new Resource.Builder()
                .setId(CuidAdapter.partnerFormId(database.getId()))
                .setParentId(database.getResourceId())
                .setLabel(I18N.CONSTANTS.partners())
                .setVisibleAsReference()
                .setType(ResourceType.FORM)
                .build();
    }

    private List<Resource> fetchForms(@NotNull Database database) {
        return database.getActivities().stream()
                .filter(a -> !a.isDeleted())
                .map(Activity::asResource)
                .collect(Collectors.toList());
    }

    private List<Resource> fetchMonthlyReportingSubForms(Database database) {
        return database.getActivities().stream()
                .filter(a -> !a.isDeleted())
                .filter(Activity::isClassicView)
                .filter(a -> a.getReportingFrequency() == ActivityFormDTO.REPORT_MONTHLY)
                .map(HibernateDatabaseMetaCache::buildMonthlyReportResource)
                .collect(Collectors.toList());
    }

    private static Resource buildMonthlyReportResource(Activity monthlyActivity) {
        return new Resource.Builder()
                .setId(CuidAdapter.reportingPeriodFormClass(monthlyActivity.getId()))
                .setParentId(monthlyActivity.getFormId())
                .setLabel(monthlyActivity.getName() + " Monthly Reports")
                .setVisibility(monthlyActivity.resourceVisibility())
                .setType(ResourceType.SUB_FORM)
                .build();
    }

    private List<Resource> fetchSubForms(List<Resource> formResources) {
        Map<ResourceId,Resource> resourceMap = formResources.stream().collect(Collectors.toMap(Resource::getId, r -> r));
        return formStorageProvider
                .getFormClasses(resourceMap.keySet())
                .values().stream()
                .filter(formClass -> resourceMap.containsKey(formClass.getId()))
                .flatMap(formClass -> extractSubFormResources(resourceMap.get(formClass.getId()), formClass))
                .collect(Collectors.toList());
    }

    private static Stream<Resource> extractSubFormResources(@NotNull Resource formResource, @NotNull FormClass formClass) {
        return formClass.getFields().stream()
                .filter(field -> field.getType() instanceof SubFormReferenceType)
                .map(subFormRef -> buildSubFormResource(formResource, subFormRef));
    }

    private static Resource buildSubFormResource(Resource parentFormResource, FormField subFormReferenceField) {
        return new Resource.Builder()
                .setId(subFormId(subFormReferenceField))
                .setParentId(parentFormResource.getId())
                .setLabel(subFormReferenceField.getLabel())
                .setVisibility(parentFormResource.getVisibility())
                .setType(ResourceType.SUB_FORM)
                .build();
    }

    private static ResourceId subFormId(@NotNull FormField subFormField) {
        return ((SubFormReferenceType) subFormField.getType()).getClassId();
    }

    private List<Resource> fetchFolders(@NotNull Database database) {
        return entityManager.get().createQuery("SELECT f " +
                "FROM Folder f " +
                "WHERE f.database=:database", Folder.class)
                .setParameter("database", database)
                .getResultList().stream()
                .map(Folder::asResource)
                .collect(Collectors.toList());
    }

    private List<Resource> fetchLocationTypes(@NotNull Database database) {
        return entityManager.get().createQuery("SELECT lt " +
                "FROM LocationType lt " +
                "WHERE lt.database=:database " +
                "AND lt.dateDeleted IS NULL", LocationType.class)
                .setParameter("database", database)
                .getResultList().stream()
                .filter(Objects::nonNull)
                .map(lt -> buildLocationTypeResource(database, lt))
                .collect(Collectors.toList());
    }

    private static Resource buildLocationTypeResource(@NotNull Database database, @NotNull LocationType locationType) {
        return new Resource.Builder()
                .setId(CuidAdapter.locationFormClass(locationType.getId()))
                .setLabel(locationType.getName())
                .setParentId(database.getResourceId())
                .setType(ResourceType.FORM)
                .setVisibleAsReference()
                .build();
    }

    private List<RecordLock> fetchLocks(@NotNull Database database) {
        return database.getLockedPeriods().stream()
                .filter(LockedPeriod::isEnabled)
                .map(LockedPeriod::asDatabaseLock)
                .collect(Collectors.toList());
    }

}
