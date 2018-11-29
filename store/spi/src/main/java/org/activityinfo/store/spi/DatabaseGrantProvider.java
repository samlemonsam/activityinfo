package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public interface DatabaseGrantProvider {

    List<DatabaseGrant> getAllDatabaseGrantsForUser(int userId);

    List<DatabaseGrant> getAllDatabaseGrantsForDatabase(ResourceId databaseId);

    DatabaseGrant getDatabaseGrant(int userId, ResourceId databaseId);

}
