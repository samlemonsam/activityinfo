package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

public interface DatabaseMetaProvider {

    Map<ResourceId,DatabaseMeta> getDatabaseMeta(@NotNull Set<ResourceId> databaseIds);

    Map<ResourceId,DatabaseMeta> getOwnedDatabaseMeta(int ownerId);

    @Nullable DatabaseMeta getDatabaseMeta(@NotNull ResourceId databaseId);

    @Nullable DatabaseMeta getDatabaseMetaForResource(@NotNull ResourceId resourceId);

}
