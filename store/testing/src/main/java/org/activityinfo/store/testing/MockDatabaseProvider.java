package org.activityinfo.store.testing;

import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseProvider;

import java.util.List;
import java.util.Optional;

public class MockDatabaseProvider implements DatabaseProvider {

    @Override
    public Optional<UserDatabaseMeta> getDatabaseMetadata(ResourceId databaseId, int userId) {
        return Optional.of(new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseId)
                .setUserId(userId)
                .setLabel("Test Database")
                .setOwner(true)
                .setPendingTransfer(false)
                .setVersion("1")
                .build());
    }

    @Override
    public Optional<UserDatabaseMeta> getDatabaseMetadata(int databaseId, int userId) {
        return getDatabaseMetadata(CuidAdapter.databaseId(databaseId), userId);
    }

    @Override
    public List<UserDatabaseMeta> getVisibleDatabases(int userId) {
        throw new IllegalArgumentException("TODO");
    }

    @Override
    public Optional<UserDatabaseMeta> getDatabaseMetadataByResource(ResourceId resourceId, int userId) {
        throw new IllegalArgumentException("TODO");
    }
}
