package org.activityinfo.server.database.hibernate;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.functions.EqualFunction;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.activityinfo.store.spi.DatabaseGrantCache;
import org.activityinfo.model.database.DatabaseGrantKey;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <p>Caching mechanism for DatabaseGrants. The cache has three components:
 * <ol>
 *     <li>Request-level in-memory cache for DatabaseGrant, using Guava LoadingCache as backing cache.</li>
 *     <li>Distributed in-memory cache, using Appengine Memcache.</li>
 *     <li>Database Loader, using Hibernate EntityManager.</li>
 * </ol>
 *     The cache will attempt to retrieve DatabaseGrant from the request cache first, then Memcache, and if that
 *     fails then loads and builds the DatabaseGrant from the MySQL Database. DatabaseGrants, when constructed, will be
 *     stored in Memcache and request cache.
 * </p>
 *
 * <p>DatabaseGrants are keyed in the request cache by a DatabaseGrantKey.</p>
 */
public class HibernateDatabaseGrantCache implements DatabaseGrantCache {

    private static final Logger LOGGER = Logger.getLogger(HibernateDatabaseGrantCache.class.getName());

    private static final String CACHE_PREFIX = "dbGrant";
    private static final String CACHE_VERSION = "1";

    private static final long MAX_CACHE_SIZE = 1000;
    private static final long EXPIRES_IN = 10;

    private final Provider<EntityManager> entityManager;
    private final MemcacheService memcacheService;

    private final LoadingCache<DatabaseGrantKey,Optional<DatabaseGrant>> cache;

    @Inject
    public HibernateDatabaseGrantCache(Provider<EntityManager> entityManager,
                                       MemcacheService memcacheService) {
        this.entityManager = entityManager;
        this.memcacheService = memcacheService;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterAccess(EXPIRES_IN, TimeUnit.MINUTES)
                .build(buildCacheLoader());
    }

    @Override
    public Optional<DatabaseGrant> load(DatabaseGrantKey toLoad) {
        try {
            return cache.get(toLoad);
        } catch (Exception loadFailure) {
            throw new RuntimeException(loadFailure);
        }
    }

    @Override
    public Map<DatabaseGrantKey,DatabaseGrant> loadAll(Set<DatabaseGrantKey> toLoad) {
        try {
            return cache.getAll(toLoad).entrySet().stream()
                    .filter(optionalGrant -> optionalGrant.getValue().isPresent())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            grant -> grant.getValue().get()));
        } catch (Exception loadFailure) {
            throw new RuntimeException(loadFailure);
        }
    }

    private CacheLoader<DatabaseGrantKey,Optional<DatabaseGrant>> buildCacheLoader() {
        return new CacheLoader<DatabaseGrantKey, Optional<DatabaseGrant>>() {
            @Override
            public Optional<DatabaseGrant> load(DatabaseGrantKey key) throws Exception {
                // Load versions
                Map<DatabaseGrantKey,Long> toLoad = queryGrantVersions(Collections.singleton(key));
                if (toLoad.isEmpty()) {
                    return Optional.empty();
                }

                // Load from Memcache
                Map<DatabaseGrantKey,Optional<DatabaseGrant>> loadedFromMemcache = loadFromMemcache(toLoad);
                if (!loadedFromMemcache.isEmpty()) {
                    return loadedFromMemcache.get(key);
                }

                // Load from Database
                Map<DatabaseGrantKey,DatabaseGrant> loadedFromDb = loadFromDb(toLoad.keySet());
                if (!loadedFromDb.isEmpty()) {
                    cacheToMemcache(loadedFromDb.values());
                    return Optional.of(loadedFromDb.get(key));
                }

                return Optional.empty();
            }

            @Override
            public Map<DatabaseGrantKey,Optional<DatabaseGrant>> loadAll(Iterable<? extends DatabaseGrantKey> keys) throws Exception {
                Set<DatabaseGrantKey> keySet = Sets.newHashSet(keys);

                // Load versions
                Map<DatabaseGrantKey,Long> toLoad = queryGrantVersions(keySet);
                if (toLoad.isEmpty()) {
                    return mapMissingEntries(keySet);
                }
                Map<DatabaseGrantKey,Optional<DatabaseGrant>> loaded = new HashMap<>(toLoad.size());

                // Create set of missing entries which cannot be loaded
                Set<DatabaseGrantKey> missing = Sets.difference(keySet,toLoad.keySet()).immutableCopy();

                // Load from Memcache
                loaded.putAll(loadFromMemcache(toLoad));
                loaded.keySet().forEach(toLoad::remove);
                if (toLoad.isEmpty()) {
                    loaded.putAll(mapMissingEntries(missing));
                    return loaded;
                }

                // Load from Database
                Map<DatabaseGrantKey,DatabaseGrant> loadedFromDb = loadFromDb(toLoad.keySet());
                if (!loadedFromDb.isEmpty()) {
                    cacheToMemcache(loadedFromDb.values());
                    loaded.putAll(loadedFromDb.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    grant -> Optional.of(grant.getValue()))));
                    loadedFromDb.keySet().forEach(toLoad::remove);
                }

                // Return an Optional.empty for grant(s) left in "toLoad" and for missing grants
                loaded.putAll(mapMissingEntries(toLoad.keySet()));
                loaded.putAll(mapMissingEntries(missing));

                return loaded;
            }
        };
    }

    private static Map<DatabaseGrantKey,Optional<DatabaseGrant>> mapMissingEntries(Set<DatabaseGrantKey> keySet) {
        return keySet.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> Optional.empty()));
    }

    private Map<DatabaseGrantKey,Long> queryGrantVersions(Set<DatabaseGrantKey> grantKeys) {
        return entityManager.get().createQuery("SELECT up.user.id, up.database.id, up.version " +
                "FROM UserPermission up " +
                "WHERE CONCAT(up.user.id,'" + DatabaseGrantKey.SEP + "',up.database.id) IN :grantKeys", Object[].class)
                .setParameter("grantKeys", grantKeys.stream().map(DatabaseGrantKey::toString).collect(Collectors.toSet()))
                .getResultList().stream()
                .collect(Collectors.toMap(
                        result -> DatabaseGrantKey.of((int) result[0], CuidAdapter.databaseId((int) result[1])),
                        result -> (long) result[2]));
    }

    private Map<DatabaseGrantKey,Optional<DatabaseGrant>> loadFromMemcache(Map<DatabaseGrantKey,Long> toFetch) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseGrant(s) from Memcache", toFetch.size()));
        Map<DatabaseGrantKey,Optional<DatabaseGrant>> loaded = new HashMap<>(toFetch.size());
        Map<String,DatabaseGrantKey> fetchKeys = toFetch.entrySet().stream()
                .collect(Collectors.toMap(
                        fetch -> memcacheKey(fetch.getKey().getUserId(), fetch.getKey().getDatabaseId(), fetch.getValue()),
                        Map.Entry::getKey));
        try {
            Map<String,Object> cached = memcacheService.getAll(fetchKeys.keySet());
            loaded.putAll(cached.entrySet().stream()
                    .collect(Collectors.toMap(
                            cachedGrant -> fetchKeys.get(cachedGrant.getKey()),
                            cachedGrant -> Optional.of(deserialize((String) cachedGrant.getValue())))));
            LOGGER.info(() -> String.format("Fetched %d/%d DatabaseGrant(s) from Memcache", loaded.size(), fetchKeys.size()));
        } catch (Exception ignorable) {
            // Memcache load failed, but we can still retrieve from database
            LOGGER.info(() -> String.format("Failed to fetch %d DatabaseGrant(s) from Memcache", fetchKeys.size()));
        }
        return loaded;
    }

    private DatabaseGrant deserialize(String grant) {
        return DatabaseGrant.fromJson(Json.parse(grant));
    }

    private void cacheToMemcache(Collection<DatabaseGrant> grants) {
        LOGGER.info(() -> String.format("Caching %d DatabaseGrant(s) to Memcache", grants.size()));
        Map<String,String> toCache = grants.stream()
                .collect(Collectors.toMap(
                        grant -> memcacheKey(grant.getUserId(), grant.getDatabaseId(), grant.getVersion()),
                        grant -> grant.toJson().toJson()));
        try {
            memcacheService.putAll(toCache);
        } catch (Exception ignorable) {
            // Caching failed, but is not terminal
            LOGGER.severe(String.format("Caching failed for %d DatabaseGrant(s) to Memcache", grants.size()));
        }
    }

    static String memcacheKey(int userId, ResourceId databaseId, long grantVersion) {
        return String.format("%s:%s_%d:%s:%d", CACHE_PREFIX, CACHE_VERSION, userId, databaseId.asString(), grantVersion);
    }

    private Map<DatabaseGrantKey,DatabaseGrant> loadFromDb(Set<DatabaseGrantKey> toFetch) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseGrant(s) from MySqlDatabase", toFetch.size()));
        return entityManager.get().createQuery("SELECT up " +
                "FROM UserPermission up " +
                "WHERE concat(up.user.id, '" + DatabaseGrantKey.SEP + "', up.database.id) IN :grantKeys", UserPermission.class)
                .setParameter("grantKeys", toFetch.stream().map(DatabaseGrantKey::toString).collect(Collectors.toSet()))
                .getResultList().stream()
                .filter(Objects::nonNull)
                .map(HibernateDatabaseGrantCache::buildDatabaseGrant)
                .collect(Collectors.toMap(
                        grant -> DatabaseGrantKey.of(grant.getUserId(), grant.getDatabaseId()),
                        grant -> grant));
    }

    private static DatabaseGrant buildDatabaseGrant(@NotNull UserPermission userPermission) {
        return new DatabaseGrant.Builder()
                .setUserId(userPermission.getUser().getId())
                .setDatabaseId(CuidAdapter.databaseId(userPermission.getDatabase().getId()))
                .setVersion(userPermission.getVersion())
                .addGrants(buildGrants(userPermission))
                .build();
    }

    public static List<GrantModel> buildGrants(@NotNull UserPermission userPermission) {
        List<GrantModel> grants = new ArrayList<>();
        if(!userPermission.isAllowView()) {
            return grants;
        }
        GrantModel rootGrant = buildRootGrant(CuidAdapter.databaseId(userPermission.getDatabase().getId()), userPermission);
        GrantModel partnerFormGrant = buildPartnerFormGrant(userPermission);
        grants.add(partnerFormGrant);
        if (userPermission.getModel() == null) {
            grants.add(rootGrant);
            return grants;
        }
        JsonValue modelObject = Json.parse(userPermission.getModel());
        grants.addAll(buildGrantsFromModel(modelObject, rootGrant));
        // We only add a "root" grant if and only if the user has no specified grants
        if (grants.isEmpty()) {
            grants.add(rootGrant);
        }
        return grants;
    }

    private static GrantModel buildRootGrant(@NotNull ResourceId databaseId, @NotNull UserPermission userPermission) {
        GrantModel.Builder databaseGrant = new GrantModel.Builder();
        databaseGrant.setResourceId(databaseId);
        setOperations(databaseGrant, userPermission);
        return databaseGrant.build();
    }

    private static GrantModel buildPartnerFormGrant(UserPermission userPermission) {
        GrantModel.Builder partnerFormGrant = new GrantModel.Builder();
        partnerFormGrant.setResourceId(CuidAdapter.partnerFormId(userPermission.getDatabase().getId()));
        setPartnerOperations(partnerFormGrant, userPermission);
        return partnerFormGrant.build();
    }

    private static void setPartnerOperations(GrantModel.Builder partnerFormGrant, UserPermission userPermission) {
        if (userPermission.isAllowViewAll()) {
            partnerFormGrant.addOperation(Operation.VIEW);
        } else {
            partnerFormGrant.addOperation(Operation.VIEW, getPartnerFilter(userPermission));
        }
        if (userPermission.isAllowManageAllUsers()) {
            partnerFormGrant.addOperation(Operation.CREATE_RECORD);
            partnerFormGrant.addOperation(Operation.EDIT_RECORD);
            partnerFormGrant.addOperation(Operation.DELETE_RECORD);
            partnerFormGrant.addOperation(Operation.EXPORT_RECORDS);
        } else if (userPermission.isAllowManageUsers()) {
            partnerFormGrant.addOperation(Operation.CREATE_RECORD, getPartnerFilter(userPermission));
            partnerFormGrant.addOperation(Operation.EDIT_RECORD, getPartnerFilter(userPermission));
            partnerFormGrant.addOperation(Operation.DELETE_RECORD, getPartnerFilter(userPermission));
            partnerFormGrant.addOperation(Operation.EXPORT_RECORDS, getPartnerFilter(userPermission));
        }
    }

    private static List<GrantModel> buildGrantsFromModel(@NotNull JsonValue modelObject, @NotNull GrantModel rootGrant) {
        if (!modelObject.hasKey("grants")) {
            LOGGER.severe(() -> "Could not parse permissions model: " + modelObject);
            throw new UnsupportedOperationException("Unsupported model");
        }
        List<GrantModel> grants = new ArrayList<>();
        for (JsonValue grant : modelObject.get("grants").values()) {
            GrantModel grantModel = buildGrantModel(grant, rootGrant);
            grants.add(grantModel);
        }
        return grants;
    }

    // Legacy Model only specifies folderIds user is limited to,
    // and we need to duplicate the operations of the root grant for each
    private static GrantModel buildGrantModel(@NotNull JsonValue object, @NotNull GrantModel rootGrant) {
        GrantModel.Builder builder = new GrantModel.Builder();
        JsonValue resourceId = object.hasKey("folderId") ? object.get("folderId") : object.get("resourceId");
        builder.setResourceId(ResourceId.valueOf(resourceId.asString()));
        for (Operation operation : rootGrant.getOperations()) {
            if (rootGrant.getFilter(operation).isPresent()) {
                builder.addOperation(operation, rootGrant.getFilter(operation).get());
            } else {
                builder.addOperation(operation);
            }
        }
        return builder.build();
    }

    private static void setOperations(@NotNull GrantModel.Builder grantModel, @NotNull UserPermission userPermission) {
        if(userPermission.isAllowViewAll()) {
            grantModel.addOperation(Operation.VIEW);
        } else if(userPermission.isAllowView()) {
            grantModel.addOperation(Operation.VIEW, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowCreateAll()) {
            grantModel.addOperation(Operation.CREATE_RECORD);
        } else if(userPermission.isAllowCreate()) {
            grantModel.addOperation(Operation.CREATE_RECORD, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowEditAll()) {
            grantModel.addOperation(Operation.EDIT_RECORD);
        } else if(userPermission.isAllowEdit()) {
            grantModel.addOperation(Operation.EDIT_RECORD, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowDeleteAll()) {
            grantModel.addOperation(Operation.DELETE_RECORD);
        } else if(userPermission.isAllowDelete()) {
            grantModel.addOperation(Operation.DELETE_RECORD, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowManageAllUsers()) {
            grantModel.addOperation(Operation.MANAGE_USERS);
        } else if(userPermission.isAllowManageUsers()) {
            grantModel.addOperation(Operation.MANAGE_USERS, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowDesign()) {
            grantModel.addOperation(Operation.CREATE_RESOURCE);
            grantModel.addOperation(Operation.EDIT_RESOURCE);
            grantModel.addOperation(Operation.DELETE_RESOURCE);
            grantModel.addOperation(Operation.LOCK_RECORDS);
            grantModel.addOperation(Operation.MANAGE_TARGETS);
        }
        if (userPermission.isAllowExport()) {
            if (userPermission.isAllowViewAll()) {
                grantModel.addOperation(Operation.EXPORT_RECORDS);
            } else {
                grantModel.addOperation(Operation.EXPORT_RECORDS, getPartnerFilter(userPermission));
            }
        }
    }

    private static String getPartnerFilter(@NotNull UserPermission userPermission) {
        List<FormulaNode> partnerNodes = mapPartnersToNodes(userPermission);
        if (partnerNodes.size() == 1) {
            return Iterables.getOnlyElement(partnerNodes).asExpression();
        }
        return Formulas.anyTrue(partnerNodes).asExpression();
    }

    private static List<FormulaNode> mapPartnersToNodes(@NotNull UserPermission userPermission) {
        return userPermission.getPartners().stream()
                .map(partner -> partnerNode(userPermission.getDatabase().getId(), partner.getId()))
                .collect(Collectors.toList());
    }

    private static FormulaNode partnerNode(int databaseId, int partnerId) {
        SymbolNode partnerForm = new SymbolNode(CuidAdapter.partnerFormId(databaseId));
        ConstantNode partnerRecord = new ConstantNode(CuidAdapter.partnerRecordId(partnerId).asString());
        return new FunctionCallNode(EqualFunction.INSTANCE, partnerForm, partnerRecord);
    }


}
