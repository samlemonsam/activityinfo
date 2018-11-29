package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import java.util.Map;
import java.util.Set;

public interface DatabaseMetaProvider {

    Map<ResourceId,DatabaseMeta> getDatabaseMeta(Set<ResourceId> databaseIds);

    Map<ResourceId,DatabaseMeta> getOwnedDatabaseMeta(int ownerId);

    DatabaseMeta getDatabaseMeta(ResourceId databaseId);

}
