package org.activityinfo.server.endpoint.rest;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormCatalog;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides the CatalogEntries for a database
 */
public class DatabaseCatalogProvider implements FormCatalog {

    private final UserDatabaseProvider userDatabaseProvider;
    private final GeoDatabaseProvider geoDatabaseProvider;

    @Inject
    public DatabaseCatalogProvider(UserDatabaseProvider userDatabaseProvider,
                                   GeoDatabaseProvider geoDatabaseProvider) {
        this.userDatabaseProvider = userDatabaseProvider;
        this.geoDatabaseProvider = geoDatabaseProvider;
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
        UserDatabaseMeta db = userDatabaseProvider.queryByResource(parentResourceId, userId);
        return db.findCatalogEntries(ResourceId.valueOf(parentId));
    }

    private List<CatalogEntry> findDatabaseEntries(int userId) {
        return userDatabaseProvider.fetchVisibleDatabaseMeta(userId).stream()
                .map(DatabaseCatalogProvider::databaseEntry)
                .collect(Collectors.toList());
    }

    private static CatalogEntry databaseEntry(UserDatabaseMeta userDatabaseMeta) {
        return new CatalogEntry(userDatabaseMeta.getDatabaseId().asString(),
                userDatabaseMeta.getLabel(),
                CatalogEntryType.FOLDER);
    }

}
