package org.activityinfo.server.database.hibernate;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.formula.ConstantNode;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FunctionCallNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.functions.EqualFunction;
import org.activityinfo.model.formula.functions.OrFunction;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.util.Pair;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.activityinfo.store.spi.DatabaseGrantProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HibernateDatabaseGrantProvider implements DatabaseGrantProvider {

    private static final Logger LOGGER = Logger.getLogger(HibernateDatabaseGrantProvider.class.getName());

    private static final String CACHE_PREFIX = "dbGrant";
    private static final String CACHE_VERSION = "1";

    private final Provider<EntityManager> entityManager;
    private final MemcacheService memcacheService;

    @Inject
    public HibernateDatabaseGrantProvider(Provider<EntityManager> entityManager,
                                          MemcacheService memcacheService) {
        this.entityManager = entityManager;
        this.memcacheService = memcacheService;
    }

    @Override
    public Optional<DatabaseGrant> getDatabaseGrant(int userId, @NotNull ResourceId databaseId) {
        Map<ResourceId,Long> grantVersion = queryGrantVersions(userId, Collections.singleton(databaseId));
        if (grantVersion.isEmpty()) {
            return Optional.empty();
        }
        Map<ResourceId,DatabaseGrant> loaded = loadFromCache(userId, grantVersion);
        if (!loaded.isEmpty()) {
            return Optional.of(loaded.get(databaseId));
        }
        loaded = loadFromDb(userId, grantVersion.keySet());
        if (loaded.isEmpty()) {
            return Optional.empty();
        }
        cacheToMemcache(loaded.values());
        return Optional.of(loaded.get(databaseId));
    }

    @Override
    public List<DatabaseGrant> getAllDatabaseGrantsForUser(int userId) {
        Set<ResourceId> grantedDatabases = queryGrantedDatabases(userId);
        if (grantedDatabases.isEmpty()) {
            return Collections.emptyList();
        }
        Map<ResourceId,Long> grantVersions = queryGrantVersions(userId, grantedDatabases);
        Map<ResourceId,DatabaseGrant> loaded = loadFromCache(userId, grantVersions);
        if (loaded.size() == grantVersions.size()) {
            return new ArrayList<>(loaded.values());
        }
        loaded.forEach((dbId,grant) -> grantVersions.remove(dbId));
        Map<ResourceId,DatabaseGrant> loadedFromDb = loadFromDb(userId, grantVersions.keySet());
        if (!loadedFromDb.isEmpty()) {
            cacheToMemcache(loadedFromDb.values());
            loaded.putAll(loadedFromDb);
        }
        return new ArrayList<>(loaded.values());
    }

    @Override
    public List<DatabaseGrant> getAllDatabaseGrantsForDatabase(@NotNull ResourceId databaseId) {
        Set<Integer> users = queryGrantedUsers(databaseId);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer,Long> grantVersions = queryGrantVersions(users, databaseId);
        Map<Integer,DatabaseGrant> loaded = loadFromCache(databaseId, grantVersions);
        if (loaded.size() == grantVersions.size()) {
            return new ArrayList<>(loaded.values());
        }
        loaded.forEach((userId,grant) -> grantVersions.remove(userId));
        Map<Integer,DatabaseGrant> loadedFromDb = loadFromDb(databaseId, grantVersions.keySet());
        cacheToMemcache(loadedFromDb.values());
        loaded.putAll(loadedFromDb);
        return new ArrayList<>(loaded.values());
    }

    private Set<Integer> queryGrantedUsers(ResourceId databaseId) {
        return new HashSet<>(entityManager.get().createQuery("SELECT up.user.id " +
                "FROM UserPermission up " +
                "WHERE up.database.id=:databaseId " +
                "AND up.allowView = TRUE", Integer.class)
                .setParameter("databaseId", CuidAdapter.getLegacyIdFromCuid(databaseId))
                .getResultList());
    }

    private Set<ResourceId> queryGrantedDatabases(int userId) {
        return entityManager.get().createQuery("SELECT up.database.id " +
                "FROM UserPermission up " +
                "WHERE up.user.id=:userId " +
                "AND up.allowView = TRUE", Integer.class)
                .setParameter("userId", userId)
                .getResultList().stream()
                .map(CuidAdapter::databaseId)
                .collect(Collectors.toSet());
    }

    private Map<ResourceId,Long> queryGrantVersions(int userId, Set<ResourceId> databaseIds) {
        return databaseIds.stream()
                .map(dbId -> new Pair<>(dbId, queryGrantVersion(userId, dbId)))
                .filter(dbGrantVersion -> dbGrantVersion.getSecond() != null)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private Map<Integer,Long> queryGrantVersions(Set<Integer> userIds, ResourceId databaseId) {
        return userIds.stream()
                .map(userId -> new Pair<>(userId, queryGrantVersion(userId, databaseId)))
                .filter(userGrantVersion -> userGrantVersion.getSecond() != null)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private Long queryGrantVersion(int userId, ResourceId databaseId) {
        try {
            return entityManager.get().createQuery("SELECT up.version " +
                    "FROM UserPermission up " +
                    "WHERE up.user.id=:userId " +
                    "AND up.database.id=:databaseId", Long.class)
                    .setParameter("userId", userId)
                    .setParameter("databaseId", CuidAdapter.getLegacyIdFromCuid(databaseId))
                    .getSingleResult();
        } catch (NoResultException noGrant) {
            // User has no grant, so return null
            return null;
        }
    }

    private Map<ResourceId,DatabaseGrant> loadFromCache(int userId, Map<ResourceId,Long> databaseGrantVersions) {
        Map<String,ResourceId> fetchKeys = databaseGrantVersions.entrySet().stream()
                .collect(Collectors.toMap(
                        dbGrant -> memcacheKey(userId, dbGrant.getKey(), dbGrant.getValue()),
                        Map.Entry::getKey));
        Map<String,DatabaseGrant> loaded = loadFromMemcache(fetchKeys.keySet());
        return loaded.entrySet().stream()
                .collect(Collectors.toMap(
                        cachedGrant -> fetchKeys.get(cachedGrant.getKey()),
                        Map.Entry::getValue));
    }

    private Map<Integer,DatabaseGrant> loadFromCache(ResourceId databaseId, Map<Integer,Long> userGrantVersions) {
        Map<String,Integer> fetchKeys = userGrantVersions.entrySet().stream()
                .collect(Collectors.toMap(
                        userGrant -> memcacheKey(userGrant.getKey(), databaseId, userGrant.getValue()),
                        Map.Entry::getKey));
        Map<String,DatabaseGrant> loaded = loadFromMemcache(fetchKeys.keySet());
        return loaded.entrySet().stream()
                .collect(Collectors.toMap(
                        cachedGrant -> fetchKeys.get(cachedGrant.getKey()),
                        Map.Entry::getValue));
    }

    private Map<String,DatabaseGrant> loadFromMemcache(Set<String> fetchKeys) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseGrant(s) from Memcache", fetchKeys.size()));
        Map<String,DatabaseGrant> loaded = new HashMap<>();
        try {
            Map<String,Object> cached = memcacheService.getAll(fetchKeys);
            loaded.putAll(cached.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            cachedGrant -> deserialize((String) cachedGrant.getValue()))));
        } catch (Exception ignorable) {
            // Memcache load failed, but we can still retrieve from database
            LOGGER.info(() -> String.format("Failed to fetch %d DatabaseGrant(s) from Memcache", fetchKeys.size()));
        }
        LOGGER.info(() -> String.format("Fetched %d/%d DatabaseGrant(s) from Memcache", loaded.size(), fetchKeys.size()));
        return loaded;
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

    private Map<ResourceId,DatabaseGrant> loadFromDb(int userId, Set<ResourceId> databaseIds) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseGrant(s) from MySqlDatabase", databaseIds.size()));
        Set<Integer> legacyIds = databaseIds.stream().map(CuidAdapter::getLegacyIdFromCuid).collect(Collectors.toSet());
        return entityManager.get().createQuery("SELECT up " +
                "FROM UserPermission up " +
                "WHERE up.user.id=:userId " +
                "AND up.database.id IN :databaseIds", UserPermission.class)
                .setParameter("userId", userId)
                .setParameter("databaseIds", legacyIds)
                .getResultList().stream()
                .filter(Objects::nonNull)
                .map(HibernateDatabaseGrantProvider::buildDatabaseGrant)
                .collect(Collectors.toMap(DatabaseGrant::getDatabaseId, grant -> grant));
    }

    private Map<Integer,DatabaseGrant> loadFromDb(ResourceId databaseId, Set<Integer> userIds) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseGrant(s) from MySqlDatabase", userIds.size()));
        return entityManager.get().createQuery("SELECT up " +
                "FROM UserPermission up " +
                "WHERE up.user.id IN :userIds " +
                "AND up.database.id=:databaseId", UserPermission.class)
                .setParameter("userIds", userIds)
                .setParameter("databaseId", CuidAdapter.getLegacyIdFromCuid(databaseId))
                .getResultList().stream()
                .filter(Objects::nonNull)
                .map(HibernateDatabaseGrantProvider::buildDatabaseGrant)
                .collect(Collectors.toMap(DatabaseGrant::getUserId, grant -> grant));
    }

    private DatabaseGrant deserialize(String grant) {
        return DatabaseGrant.fromJson(Json.parse(grant));
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
            partnerFormGrant.addOperation(Operation.VIEW, getUserGroupFilter(userPermission));
        }
        if (userPermission.isAllowManageAllUsers()) {
            partnerFormGrant.addOperation(Operation.CREATE_RECORD);
            partnerFormGrant.addOperation(Operation.EDIT_RECORD);
            partnerFormGrant.addOperation(Operation.DELETE_RECORD);
            partnerFormGrant.addOperation(Operation.EXPORT_RECORDS);
        } else if (userPermission.isAllowManageUsers()) {
            partnerFormGrant.addOperation(Operation.CREATE_RECORD, getUserGroupFilter(userPermission));
            partnerFormGrant.addOperation(Operation.EDIT_RECORD, getUserGroupFilter(userPermission));
            partnerFormGrant.addOperation(Operation.DELETE_RECORD, getUserGroupFilter(userPermission));
            partnerFormGrant.addOperation(Operation.EXPORT_RECORDS, getUserGroupFilter(userPermission));
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
            grantModel.addOperation(Operation.VIEW, getUserGroupFilter(userPermission));
        }
        if(userPermission.isAllowCreateAll()) {
            grantModel.addOperation(Operation.CREATE_RECORD);
        } else if(userPermission.isAllowCreate()) {
            grantModel.addOperation(Operation.CREATE_RECORD, getUserGroupFilter(userPermission));
        }
        if(userPermission.isAllowEditAll()) {
            grantModel.addOperation(Operation.EDIT_RECORD);
        } else if(userPermission.isAllowEdit()) {
            grantModel.addOperation(Operation.EDIT_RECORD, getUserGroupFilter(userPermission));
        }
        if(userPermission.isAllowDeleteAll()) {
            grantModel.addOperation(Operation.DELETE_RECORD);
        } else if(userPermission.isAllowDelete()) {
            grantModel.addOperation(Operation.DELETE_RECORD, getUserGroupFilter(userPermission));
        }
        if(userPermission.isAllowManageAllUsers()) {
            grantModel.addOperation(Operation.MANAGE_USERS);
        } else if(userPermission.isAllowManageUsers()) {
            grantModel.addOperation(Operation.MANAGE_USERS, getUserGroupFilter(userPermission));
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
                grantModel.addOperation(Operation.EXPORT_RECORDS, getUserGroupFilter(userPermission));
            }
        }
    }

    private static String getUserGroupFilter(@NotNull UserPermission userPermission) {
        List<FormulaNode> userGroupNodes = mapUserGroupsToNodes(userPermission);
        if (userGroupNodes.size() == 1) {
            return Iterables.getOnlyElement(userGroupNodes).asExpression();
        }
        return new FunctionCallNode(OrFunction.INSTANCE, userGroupNodes).asExpression();
    }

    private static FormulaNode userGroupNode(int databaseId, int userGroupId) {
        SymbolNode userGroupForm = new SymbolNode(CuidAdapter.partnerFormId(databaseId));
        ConstantNode userGroupRecord = new ConstantNode(CuidAdapter.partnerRecordId(userGroupId).asString());
        return new FunctionCallNode(EqualFunction.INSTANCE, userGroupForm, userGroupRecord);
    }

    private static List<FormulaNode> mapUserGroupsToNodes(@NotNull UserPermission userPermission) {
        return userPermission.getUserGroups().stream()
                .map(userGroup -> userGroupNode(userPermission.getDatabase().getId(), userGroup.getId()))
                .collect(Collectors.toList());
    }

}
