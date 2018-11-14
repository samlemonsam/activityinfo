package org.activityinfo.store.testing;

import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseProvider;

public class MockDatabaseProvider implements DatabaseProvider {

    @Override
    public UserDatabaseMeta getDatabaseMetadata(ResourceId databaseId, int userId) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseId)
                .setUserId(userId)
                .setLabel("Test Database")
                .setOwner(true)
                .setPendingTransfer(false)
                .setVersion("1")
                .build();
    }

    @Override
    public UserDatabaseMeta getDatabaseMetadata(int databaseId, int userId) {
        return getDatabaseMetadata(CuidAdapter.databaseId(databaseId), userId);
    }
}
