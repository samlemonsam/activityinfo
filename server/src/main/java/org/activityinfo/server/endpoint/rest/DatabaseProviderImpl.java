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
package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.database.*;
import org.activityinfo.model.formula.ConstantNode;
import org.activityinfo.model.formula.FunctionCallNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.functions.EqualFunction;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseProviderImpl implements DatabaseProvider {

    private static final Logger LOGGER = Logger.getLogger(DatabaseProviderImpl.class.getName());

    private final Provider<EntityManager> entityManager;
    private final GeoDatabaseProvider geoDbProvider;

    @Inject
    public DatabaseProviderImpl(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
        this.geoDbProvider = new GeoDatabaseProvider(entityManager);
    }

    @Override
    public List<UserDatabaseMeta> getVisibleDatabases(int userId) {
        return fetchVisibleDatabases(userId).stream()
                .map(db -> buildMetadata(db, CuidAdapter.databaseId(db.getId()), userId))
                .collect(Collectors.toList());
    }

    private List<Database> fetchVisibleDatabases(int userId) {
        List<Database> databases = new ArrayList<>();
        databases.addAll(queryAssignedDatabases(userId));
        databases.addAll(queryOwnedDatabases(userId));
        return databases;
    }

    private List<Database> queryAssignedDatabases(int userId) {
        return entityManager.get().createQuery("select up.database " +
                "from UserPermission up " +
                "where up.userId = :userId " +
                "and up.allowView", Database.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    private List<Database> queryOwnedDatabases(int userId) {
        return entityManager.get().createQuery("select db " +
                "from Database db " +
                "where db.owner.id = :userId " +
                "and db.dataDeleted is null", Database.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public UserDatabaseMeta getDatabaseMetadataByResource(ResourceId resourceId, int userId) {
        switch(resourceId.getDomain()) {
            case CuidAdapter.DATABASE_DOMAIN:
                return getDatabaseMetadata(resourceId, userId);
            case CuidAdapter.ACTIVITY_DOMAIN:
                return getDatabaseMetadataForForm(resourceId, userId);
            case CuidAdapter.FOLDER_DOMAIN:
                return getDatabaseMetadataForFolder(resourceId, userId);
            default:
                throw new IllegalArgumentException("Cannot fetch UserDatabaseMeta for Resource: " + resourceId.toString());
        }
    }

    private UserDatabaseMeta getDatabaseMetadataForForm(ResourceId formId, int userId) {
        Database db = queryDatabaseByForm(formId);
        if (db == null) {
            throw new IllegalArgumentException("Cannot fetch UserDatabaseMeta for Form " + formId.toString());
        }
        return buildMetadata(db, CuidAdapter.databaseId(db.getId()), userId);
    }

    private UserDatabaseMeta getDatabaseMetadataForFolder(ResourceId folderId, int userId) {
        Database db = queryDatabaseByFolder(folderId);
        if (db == null) {
            throw new IllegalArgumentException("Cannot fetch UserDatabaseMeta for Folder " + folderId.toString());
        }
        return buildMetadata(db, CuidAdapter.databaseId(db.getId()), userId);
    }

    @Override
    public UserDatabaseMeta getDatabaseMetadata(ResourceId databaseId, int userId) {
        if(geoDbProvider.accept(databaseId)) {
            return geoDbProvider.queryGeoDb(userId);
        } else {
            return queryMySQLDatabase(databaseId, userId);
        }
    }

    @Override
    public UserDatabaseMeta getDatabaseMetadata(int databaseId, int userId) {
        return getDatabaseMetadata(CuidAdapter.databaseId(databaseId), userId);
    }

    private UserDatabaseMeta queryMySQLDatabase(ResourceId databaseId, int userId) {
        Database database = queryDatabase(databaseId);
        if (database == null) {
            return metaBuilder(databaseId, userId).build();
        }
        return buildMetadata(database, databaseId, userId);
    }

    private UserDatabaseMeta buildMetadata(Database database, ResourceId databaseId, int userId) {
        UserDatabaseMeta.Builder meta = metaBuilder(databaseId, userId);
        meta.setLabel(database.getName());
        meta.setPublished(false);
        meta.addResources(fetchResources(database));
        meta.addLocks(fetchLocks(database));
        if (database.getOwner().getId() == userId) {
            ownerPermissions(database, meta);
        } else {
            userPermissions(database, userId, meta);
        }
        return meta.build();
    }

    private UserDatabaseMeta.Builder metaBuilder(ResourceId databaseId, int userId) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseId)
                .setUserId(userId);
    }

    private Database queryDatabase(ResourceId databaseId) {
        return entityManager.get().find(Database.class, CuidAdapter.getLegacyIdFromCuid(databaseId));
    }

    private Database queryDatabaseByForm(ResourceId formId) {
        try {
            return entityManager.get().createQuery("select form.database " +
                    "from Activity form " +
                    "where form.id = :formId", Database.class)
                    .setParameter("formId", CuidAdapter.getLegacyIdFromCuid(formId))
                    .getSingleResult();
        } catch (NoResultException noResult) {
            return null;
        }
    }

    private Database queryDatabaseByFolder(ResourceId folderId) {
        try {
            return entityManager.get().createQuery("select folder.database " +
                    "from Folder folder " +
                    "where folder.id = :folderId", Database.class)
                    .setParameter("folderId", CuidAdapter.getLegacyIdFromCuid(folderId))
                    .getSingleResult();
        } catch (NoResultException noResult) {
            return null;
        }
    }

    private List<Resource> fetchResources(Database database) {
        Stream<Resource> formResources = fetchForms(database);
        Stream<Resource> folderResources = fetchFolders(database);
        return Stream.concat(formResources, folderResources).collect(Collectors.toList());
    }

    private Stream<Resource> fetchForms(Database database) {
        return database.getActivities().stream()
                .filter(a -> !a.isDeleted())
                .map(Activity::asResource);
    }

    private Stream<Resource> fetchFolders(Database database) {
        return entityManager.get()
                .createQuery("select f from Folder f where f.database = :database", Folder.class)
                .setParameter("database", database)
                .getResultList()
                .stream()
                .map(Folder::asResource);
    }

    public List<RecordLock> fetchLocks(Database database) {
        return entityManager.get()
                .createQuery("select k from LockedPeriod k where k.database = :database", LockedPeriod.class)
                .setParameter("database", database)
                .getResultList()
                .stream()
                .filter(LockedPeriod::isEnabled)
                .map(LockedPeriod::asDatabaseLock)
                .collect(Collectors.toList());
    }

    private void ownerPermissions(Database database, UserDatabaseMeta.Builder meta) {
        meta.setOwner(true);
        meta.setVersion(Long.toString(database.getVersion()));
        meta.setPendingTransfer(database.hasPendingTransfer());
    }

    private void userPermissions(Database database, int userId, UserDatabaseMeta.Builder meta) {
        Optional<UserPermission> userPermission = fetchUserPermission(database, userId);
        if (userPermission.isPresent()) {
            meta.addGrants(fetchGrants(CuidAdapter.databaseId(database.getId()), userPermission.get()));
            meta.setVersion(database.getVersion() + UserDatabaseMeta.VERSION_SEP + userPermission.get().getVersion());
        } else {
            meta.setVersion(Long.toString(database.getVersion()));
        }
    }

    private Optional<UserPermission> fetchUserPermission(Database database, int userId) {
        try {
            return Optional.of(queryUserPermission(database, userId));
        } catch (NoResultException noPermission) {
            return Optional.empty();
        }
    }

    private UserPermission queryUserPermission(Database database, int userId) {
        return entityManager.get()
                .createQuery("select u from UserPermission u where u.user.id = :userId and u.database = :db",
                        UserPermission.class)
                .setParameter("userId", userId)
                .setParameter("db", database)
                .getSingleResult();
    }

    private List<GrantModel> fetchGrants(ResourceId databaseId, UserPermission userPermission) {
        List<GrantModel> grants = new ArrayList<>();
        if(!userPermission.isAllowView()) {
            return grants;
        }
        grants.add(buildDatabaseGrant(databaseId, userPermission));
        if (userPermission.getModel() == null) {
            return grants;
        }
        JsonValue modelObject = Json.parse(userPermission.getModel());
        grants.addAll(buildGrantsFromModel(modelObject));
        return grants;
    }

    private GrantModel buildDatabaseGrant(ResourceId databaseId, UserPermission userPermission) {
        GrantModel.Builder databaseGrant = new GrantModel.Builder();
        databaseGrant.setResourceId(databaseId);
        setOperations(databaseGrant, userPermission);
        return databaseGrant.build();
    }

    private List<GrantModel> buildGrantsFromModel(JsonValue modelObject) {
        if (!modelObject.hasKey("grants")) {
            LOGGER.severe(() -> "Could not parse permissions model: " + modelObject);
            throw new UnsupportedOperationException("Unsupported model");
        }
        List<GrantModel> grants = new ArrayList<>();
        for (JsonValue grant : modelObject.get("grants").values()) {
            GrantModel grantModel = GrantModel.fromJson(grant);
            grants.add(grantModel);
        }
        return grants;
    }

    private void setOperations(GrantModel.Builder grantModel, UserPermission userPermission) {
        if(userPermission.isAllowViewAll()) {
            grantModel.addOperation(Operation.VIEW);
        } else if(userPermission.isAllowView()) {
            grantModel.addOperation(Operation.VIEW, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowEditAll()) {
            grantModel.addOperation(Operation.EDIT_RECORD);
            grantModel.addOperation(Operation.CREATE_RECORD);
            grantModel.addOperation(Operation.DELETE_RECORD);
        } else if(userPermission.isAllowEdit()) {
            grantModel.addOperation(Operation.EDIT_RECORD, getPartnerFilter(userPermission));
            grantModel.addOperation(Operation.CREATE_RECORD, getPartnerFilter(userPermission));
            grantModel.addOperation(Operation.DELETE_RECORD, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowManageAllUsers()) {
            grantModel.addOperation(Operation.MANAGE_USERS);
        } else if(userPermission.isAllowManageUsers()) {
            grantModel.addOperation(Operation.MANAGE_USERS, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowDesign()) {
            grantModel.addOperation(Operation.CREATE_FORM);
            grantModel.addOperation(Operation.EDIT_FORM);
            grantModel.addOperation(Operation.DELETE_FORM);
        }
    }

    private String getPartnerFilter(UserPermission userPermission) {
        SymbolNode partnerForm = new SymbolNode(CuidAdapter.partnerFormId(userPermission.getDatabase().getId()));
        ConstantNode partnerRecord = new ConstantNode(CuidAdapter.partnerRecordId(userPermission.getPartner().getId()).asString());
        return new FunctionCallNode(EqualFunction.INSTANCE, partnerForm, partnerRecord).asExpression();
    }

}
