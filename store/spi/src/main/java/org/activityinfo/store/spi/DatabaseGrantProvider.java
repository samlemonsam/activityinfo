package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.resource.ResourceId;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface DatabaseGrantProvider {

    List<DatabaseGrant> getAllDatabaseGrantsForUser(int userId);

    List<DatabaseGrant> getAllDatabaseGrantsForDatabase(@NotNull ResourceId databaseId);

    Optional<DatabaseGrant> getDatabaseGrant(int userId, @NotNull ResourceId databaseId);

}
