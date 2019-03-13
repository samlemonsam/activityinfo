package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.resource.ResourceId;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Provides a users assigned permissions on a database.
 */
public interface DatabaseGrantProvider {

    /**
     * Retrieves all DatabaseGrants assigned to a given user.
     *
     * @param userId The integer id of the user
     *
     * @return List of DatabaseGrants which are currently assigned to the user
     */
    List<DatabaseGrant> getAllDatabaseGrantsForUser(int userId);

    /**
     * Retrieves all DatabaseGrants assigned on a given database.
     *
     * @param databaseId The ResourceId of the database
     *
     * @return List of DatabaseGrants which are currently assigned on the database
     */
    List<DatabaseGrant> getAllDatabaseGrantsForDatabase(@NotNull ResourceId databaseId);

    /**
     * Retrieves the DatabaseGrant for the given user and database.
     *
     * @param userId The integer id of the user
     * @param databaseId The ResourceId of the database
     *
     * @return Optional DatabaseGrant
     * <ol>
     *     <li>If the grant exists for the given user/database, then the DatabaseGrant will be returned wrapped in an
     *      Optional</li>
     *     <li>If the grant does not exist for the given user/database, then an Optional.empty() will be returned</li>
     * </ol>
     */
    Optional<DatabaseGrant> getDatabaseGrant(int userId, @NotNull ResourceId databaseId);

    /**
     * Retrieves the DatabaseGrants for the given user on a set of databases.
     *
     * @param userId The integer id of the user
     * @param databaseIds The set of database ResourceIds
     *
     * @return Map of ResourceId -> DatabaseGrant
     * <ol>
     *     <li>If the grant exists for the given user/database, then the DatabaseGrant will be returned in the map</li>
     *     <li>If the grant does not exist for the given user/database, then it will <b>not</b> be returned in the
     *      map</li>
     * </ol>
     */
    Map<ResourceId,DatabaseGrant> getDatabaseGrants(int userId, @NotNull Set<ResourceId> databaseIds);

}
