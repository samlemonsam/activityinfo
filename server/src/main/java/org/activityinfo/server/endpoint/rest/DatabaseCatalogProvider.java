package org.activityinfo.server.endpoint.rest;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormCatalog;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides the CatalogEntries for database(s) stored in MySQL.
 */
public class DatabaseCatalogProvider implements FormCatalog {

    private final UserDatabaseProvider userDatabaseProvider;
    private final GeoDatabaseProvider geoDatabaseProvider;
    private final Provider<EntityManager> entityManager;

    @Inject
    public DatabaseCatalogProvider(UserDatabaseProvider userDatabaseProvider,
                                   GeoDatabaseProvider geoDatabaseProvider,
                                   Provider<EntityManager> entityManager) {
        this.userDatabaseProvider = userDatabaseProvider;
        this.geoDatabaseProvider = geoDatabaseProvider;
        this.entityManager = entityManager;
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        return Lists.newArrayList(
                geoDbRootCatalogEntry(),
                databaseRootCatalogEntry());
    }

    private static CatalogEntry geoDbRootCatalogEntry() {
        return new CatalogEntry(GeoDatabaseProvider.GEODB_ID.asString(),
                I18N.CONSTANTS.geography(),
                CatalogEntryType.FOLDER);
    }

    private static CatalogEntry databaseRootCatalogEntry() {
        return new CatalogEntry(UserDatabaseProvider.ROOT_ID,
                I18N.CONSTANTS.databases(),
                CatalogEntryType.FOLDER);
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        // Check first for any GeoDb Resources, as these need to be specially constructed
        if (geoDatabaseProvider.accept(parentId)) {
            return geoDatabaseProvider.findCatalogEntries(parentId);
        }

        // check next if all databases visible to user are being requested
        if (parentId.equals(UserDatabaseProvider.ROOT_ID)) {
            return findDatabaseEntries(userId);
        }

        // Otherwise, the UserDatabaseMeta has sufficient data to construct the Catalog Entries
        ResourceId parentResourceId = ResourceId.valueOf(parentId);
        Optional<UserDatabaseMeta> db = userDatabaseProvider.queryUserDatabaseMetaByResource(parentResourceId, userId);
        if (!db.isPresent()) {
            return Collections.emptyList();
        }
        return db.get().findCatalogEntries(ResourceId.valueOf(parentId));
    }

    private List<CatalogEntry> findDatabaseEntries(int userId) {
        Stream<CatalogEntry> ownedDatabases = entityManager.get().createQuery("SELECT db.id, db.name " +
                "FROM Database db " +
                "WHERE db.owner.id=:ownerId " +
                "AND db.dateDeleted IS NULL", Object[].class)
            .setParameter("ownerId", userId)
            .getResultList().stream()
            .map(result -> new CatalogEntry(
                    CuidAdapter.databaseId((int) result[0]).asString(), // db.id
                    (String) result[1],                                 // db.name
                    CatalogEntryType.FOLDER));
        Stream<CatalogEntry> assignedDatabases = entityManager.get().createQuery("SELECT up.database.id, up.database.name " +
                "FROM UserPermission up " +
                "WHERE up.user.id=:userId " +
                "AND up.allowView = TRUE " +
                "AND up.database.dateDeleted IS NULL", Object[].class)
            .setParameter("userId", userId)
            .getResultList().stream()
            .map(result -> new CatalogEntry(
                    CuidAdapter.databaseId((int) result[0]).asString(),     // up.database.id
                    (String) result[1],                                     // up.database.name
                    CatalogEntryType.FOLDER));
        return Stream.concat(ownedDatabases, assignedDatabases)
                .sorted(Comparator.comparing(CatalogEntry::getLabel))
                .collect(Collectors.toList());
    }

}
