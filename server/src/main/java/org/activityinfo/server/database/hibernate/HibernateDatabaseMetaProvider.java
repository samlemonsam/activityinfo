package org.activityinfo.server.database.hibernate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.database.RecordLock;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.Activity;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Folder;
import org.activityinfo.server.database.hibernate.entity.LockedPeriod;
import org.activityinfo.store.spi.DatabaseMetaProvider;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HibernateDatabaseMetaProvider implements DatabaseMetaProvider {

    private final Provider<EntityManager> entityManager;

    @Inject
    public HibernateDatabaseMetaProvider(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public @Nullable DatabaseMeta getDatabaseMeta(@NotNull ResourceId databaseId) {
        try {
            Database database = entityManager.get().createQuery("SELECT db " +
                    "FROM Database db " +
                    "JOIN FETCH db.activities " +
                    "WHERE db.id=:databaseId", Database.class)
                    .setParameter("databaseId", CuidAdapter.getLegacyIdFromCuid(databaseId))
                    .getSingleResult();
            return buildMeta(database);
        } catch (NoResultException noDatabase) {
            return null;
        }
    }

    @Override
    public Map<ResourceId, DatabaseMeta> getDatabaseMeta(@NotNull Set<ResourceId> databaseIds) {
        if (databaseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return entityManager.get().createQuery("SELECT db " +
                "FROM Database db " +
                "JOIN FETCH db.activities " +
                "WHERE db.id IN :databaseIds", Database.class)
                .setParameter("databaseIds", databaseIds.stream().map(CuidAdapter::getLegacyIdFromCuid).collect(Collectors.toList()))
                .getResultList().stream()
                .map(this::buildMeta)
                .collect(Collectors.toMap(DatabaseMeta::getDatabaseId, dbMeta -> dbMeta));
    }

    @Override
    public Map<ResourceId, DatabaseMeta> getOwnedDatabaseMeta(int ownerId) {
        return entityManager.get().createQuery("SELECT db " +
                "FROM Database db " +
                "JOIN FETCH db.activities " +
                "WHERE db.owner.id=:ownerId", Database.class)
                .setParameter("ownerId", ownerId)
                .getResultList().stream()
                .map(this::buildMeta)
                .collect(Collectors.toMap(DatabaseMeta::getDatabaseId, dbMeta -> dbMeta));
    }

    private DatabaseMeta buildMeta(Database database) {
        return new DatabaseMeta.Builder()
                .setDatabaseId(CuidAdapter.databaseId(database.getId()))
                .setOwnerId(database.getOwner().getId())
                .setLabel(database.getName())
                .setDescription(database.getFullName())
                .setPublished(false)
                .setPendingTransfer(database.hasPendingTransfer())
                .addResources(fetchResources(database))
                .addLocks(fetchLocks(database))
                .build();
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
        return entityManager.get().createQuery("SELECT f " +
                "FROM Folder f " +
                "WHERE f.database=:database", Folder.class)
                .setParameter("database", database)
                .getResultList().stream()
                .map(Folder::asResource);
    }

    private List<RecordLock> fetchLocks(Database database) {
        return database.getLockedPeriods().stream()
                .filter(LockedPeriod::isEnabled)
                .map(LockedPeriod::asDatabaseLock)
                .collect(Collectors.toList());
    }

}
