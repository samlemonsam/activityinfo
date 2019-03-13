package org.activityinfo.store.testing;

import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.UserDatabaseProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MockUserDatabaseProvider implements UserDatabaseProvider {

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
        return getDatabaseMetadata(ResourceId.valueOf("d0000000001"), userId);
    }

    @Override
    public Map<ResourceId,UserDatabaseMeta> getDatabaseMetadata(Set<ResourceId> databaseIds, int userId) {
        return databaseIds.stream()
                .map(dbId -> getDatabaseMetadata(dbId, userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(UserDatabaseMeta::getDatabaseId, dbMeta -> dbMeta));
    }

}
