package org.activityinfo.server.endpoint.rest;

import org.activityinfo.model.database.RecordLock;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DatabaseProviderImpl implements DatabaseProvider {

    private Provider<EntityManager> entityManager;

    @Inject
    public DatabaseProviderImpl(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public UserDatabaseMeta getDatabaseMetadata(ResourceId databaseId, int userId) {

        UserDatabaseMeta.Builder meta = new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseId)
                .setUserId(userId);

        Database database = entityManager.get().find(Database.class, CuidAdapter.getLegacyIdFromCuid(databaseId));
        if(database != null) {

            if (database.getOwner().getId() == userId) {
                meta.setOwner(true);
                meta.setVersion(Long.toString(database.getVersion()));
            } else {
                Optional<UserPermission> userPermission = getUserPermission(entityManager.get(), database, userId);
                if(userPermission.isPresent()) {
                    meta.addGrants(userPermission.get().getGrants());
                    meta.setVersion(database.getVersion() + "#" + userPermission.get().getVersion());
                }
            }

            if (meta.isVisible()) {
                meta.setLabel(database.getName());
                meta.addLocks(queryLocks(database));
                meta.addResources(queryFolders(database));
                meta.addResources(queryForms(database));
            }
        }
        return meta.build();
    }



    public static Optional<UserPermission> getUserPermission(EntityManager entityManager, Database database, int userId) {
        List<UserPermission> permissions = entityManager
                .createQuery(
                        "select u from UserPermission u where u.user.id = :userId and u" +
                                ".database = :db",
                        UserPermission.class)
                .setParameter("userId", userId)
                .setParameter("db", database)
                .getResultList();


        if (permissions.isEmpty()) {
            // return a permission with nothing enabled
            return Optional.empty();

        } else {
            return Optional.of(permissions.get(0));
        }
    }

    public List<RecordLock> queryLocks(Database database) {
        return entityManager.get()
                .createQuery("select k from LockedPeriod k where k.database = :database", LockedPeriod.class)
                .setParameter("database", database)
                .getResultList()
                .stream()
                .filter(LockedPeriod::isEnabled)
                .map(LockedPeriod::asDatabaseLock)
                .collect(Collectors.toList());
    }

    public List<Resource> queryFolders(Database database) {
        return entityManager.get()
                .createQuery("select f from Folder f where f.database = :database", Folder.class)
                .setParameter("database", database)
                .getResultList()
                .stream()
                .map(Folder::asResource)
                .collect(Collectors.toList());
    }


    private List<Resource> queryForms(Database database) {
        return database.getActivities()
                .stream()
                .filter(a -> !a.isDeleted())
                .map(Activity::asResource)
                .collect(Collectors.toList());
    }

}
