package org.activityinfo.store.spi;

import org.activityinfo.model.database.UserDatabaseMeta;

/**
 * Provides metadata associated with a user's database.
 */
public interface DatabaseProvider {


  UserDatabaseMeta getDatabaseMetadata(int databaseId, int userId);

}
