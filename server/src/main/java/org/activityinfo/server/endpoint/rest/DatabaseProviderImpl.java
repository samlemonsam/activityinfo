package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseProvider;

import java.util.List;

public class DatabaseProviderImpl implements DatabaseProvider {

    private final GeoDatabaseProvider geoDbProvider;
    private final UserDatabaseProvider userDbProvider;

    @Inject
    public DatabaseProviderImpl(GeoDatabaseProvider geoDbProvider,
                                UserDatabaseProvider userDbProvider) {
        this.geoDbProvider = geoDbProvider;
        this.userDbProvider = userDbProvider;
    }

    @Override
    public UserDatabaseMeta getDatabaseMetadata(ResourceId databaseId, int userId) {
        if (geoDbProvider.accept(databaseId)) {
            return geoDbProvider.queryGeoDb(userId);
        } else {
            return userDbProvider.queryDatabaseMeta(databaseId, userId);
        }
    }

    @Override
    public UserDatabaseMeta getDatabaseMetadata(int databaseId, int userId) {
        return getDatabaseMetadata(CuidAdapter.databaseId(databaseId), userId);
    }

    @Override
    public List<UserDatabaseMeta> getVisibleDatabases(int userId) {
        return userDbProvider.queryVisibleUserDatabaseMeta(userId);
    }

    @Override
    public UserDatabaseMeta getDatabaseMetadataByResource(ResourceId resourceId, int userId) {
        if (geoDbProvider.accept(resourceId)) {
            return geoDbProvider.queryGeoDb(userId);
        } else {
            return userDbProvider.queryUserDatabaseMetaByResource(resourceId, userId);
        }
    }

}
