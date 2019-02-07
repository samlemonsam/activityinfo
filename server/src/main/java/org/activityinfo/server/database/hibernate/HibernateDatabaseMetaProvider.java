package org.activityinfo.server.database.hibernate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseMetaCache;
import org.activityinfo.store.spi.DatabaseMetaProvider;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.FormStorageProvider;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class HibernateDatabaseMetaProvider implements DatabaseMetaProvider {

    private final Provider<EntityManager> entityManager;
    private final FormStorageProvider formStorageProvider;
    private final DatabaseMetaCache cache;

    @Inject
    public HibernateDatabaseMetaProvider(Provider<EntityManager> entityManager,
                                         FormStorageProvider formStorageProvider,
                                         DatabaseMetaCache cache) {
        this.entityManager = entityManager;
        this.formStorageProvider = formStorageProvider;
        this.cache = cache;
    }

    @Override
    public Optional<DatabaseMeta> getDatabaseMeta(@NotNull ResourceId databaseId) {
        return cache.load(databaseId);
    }

    @Override
    public Map<ResourceId,DatabaseMeta> getDatabaseMeta(@NotNull Set<ResourceId> databases) {
        if (databases.isEmpty()) {
            return Collections.emptyMap();
        }
        return cache.loadAll(databases);
    }

    @Override
    public Map<ResourceId, DatabaseMeta> getOwnedDatabaseMeta(int ownerId) {
        Set<ResourceId> ownedDatabases = queryOwnedDatabaseIds(ownerId);
        if (ownedDatabases.isEmpty()) {
            return Collections.emptyMap();
        }
        return getDatabaseMeta(ownedDatabases);
    }

    @Override
    public Optional<DatabaseMeta> getDatabaseMetaForResource(@NotNull ResourceId resourceId) {
        switch(resourceId.getDomain()) {
            case CuidAdapter.DATABASE_DOMAIN:
                return getDatabaseMeta(resourceId);
            case CuidAdapter.ACTIVITY_DOMAIN:
                Optional<ResourceId> activityDatabaseId = Optional.ofNullable(queryDatabaseIdForForm(resourceId));
                return activityDatabaseId.isPresent() ? getDatabaseMeta(activityDatabaseId.get()) : Optional.empty();
            case CuidAdapter.MONTHLY_REPORT_FORM_CLASS:
                ResourceId activityFormId = CuidAdapter.activityFormClass(CuidAdapter.getLegacyIdFromCuid(resourceId));
                Optional<ResourceId> monthlyActivityDatabaseId = Optional.ofNullable(queryDatabaseIdForForm(activityFormId));
                return monthlyActivityDatabaseId.isPresent() ? getDatabaseMeta(monthlyActivityDatabaseId.get()) : Optional.empty();
            case CuidAdapter.FOLDER_DOMAIN:
                Optional<ResourceId> folderDatabaseId = Optional.ofNullable(queryDatabaseIdForFolder(resourceId));
                return folderDatabaseId.isPresent() ? getDatabaseMeta(folderDatabaseId.get()) : Optional.empty();
            case ResourceId.GENERATED_ID_DOMAIN:
                // Check for a Sub-Form Resource
                com.google.common.base.Optional<FormStorage> subForm = formStorageProvider.getForm(resourceId);
                return subForm.isPresent() ? getDatabaseMeta(subForm.get().getFormClass().getDatabaseId()) : Optional.empty();
            default:
                throw new IllegalArgumentException("Cannot fetch UserDatabaseMeta for Resource: " + resourceId.toString());
        }
    }

    private Set<ResourceId> queryOwnedDatabaseIds(int ownerId) {
        return entityManager.get().createQuery("SELECT db.id " +
                "FROM Database db " +
                "WHERE db.owner.id=:ownerId " +
                "AND db.dateDeleted IS NULL", Integer.class)
                .setParameter("ownerId", ownerId)
                .getResultList().stream()
                .map(CuidAdapter::databaseId)
                .collect(Collectors.toSet());
    }

    private @Nullable ResourceId queryDatabaseIdForForm(@NotNull ResourceId formId) {
        try {
            int dbId = entityManager.get().createQuery("select form.database.id " +
                    "from Activity form " +
                    "where form.id = :formId " +
                    "and form.dateDeleted is null", Integer.class)
                    .setParameter("formId", CuidAdapter.getLegacyIdFromCuid(formId))
                    .getSingleResult();
            return CuidAdapter.databaseId(dbId);
        } catch (NoResultException noResult) {
            return null;
        }
    }

    private @Nullable ResourceId queryDatabaseIdForFolder(@NotNull ResourceId folderId) {
        try {
            int dbId = entityManager.get().createQuery("select folder.database.id " +
                    "from Folder folder " +
                    "where folder.id = :folderId", Integer.class)
                    .setParameter("folderId", CuidAdapter.getLegacyIdFromCuid(folderId))
                    .getSingleResult();
            return CuidAdapter.databaseId(dbId);
        } catch (NoResultException noResult) {
            return null;
        }
    }

}
