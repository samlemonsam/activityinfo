package org.activityinfo.store.spi;

import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

/**
 * Provides metadata associated with a user's database.
 */
public interface DatabaseProvider {


  UserDatabaseMeta getDatabaseMetadata(ResourceId databaseId, int userId);

}
