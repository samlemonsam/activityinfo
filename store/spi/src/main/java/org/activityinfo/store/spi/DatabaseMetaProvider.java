package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DatabaseMetaProvider {

    Map<ResourceId,DatabaseMeta> getDatabaseMeta(@NotNull Set<ResourceId> databaseIds);

    Map<ResourceId,DatabaseMeta> getOwnedDatabaseMeta(int ownerId);

    Optional<DatabaseMeta> getDatabaseMeta(@NotNull ResourceId databaseId);

    Optional<DatabaseMeta> getDatabaseMetaForResource(@NotNull ResourceId resourceId);

}
