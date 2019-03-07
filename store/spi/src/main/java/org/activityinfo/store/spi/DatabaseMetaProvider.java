package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Provides metadata for a database.
 */
public interface DatabaseMetaProvider {

    /**
     * Retrieves the DatabaseMeta for a set of databases.
     *
     * @param databaseIds The set of database ResourceIds
     *
     * @return Map of ResourceId -> DatabaseMeta
     * <ol>
     *     <li>If the database exists, then the DatabaseMeta will be returned in the map</li>
     *     <li>If the database previously existed but has been deleted, then the "deleted" DatabaseMeta will be
     *      returned in the map</li>
     *     <li>If the database does not exist, then it will <b>not</b> be returned in the map</li>
     * </ol>
     */
    Map<ResourceId,DatabaseMeta> getDatabaseMeta(@NotNull Set<ResourceId> databaseIds);

    /**
     * Retrieves all DatabaseMeta owned by given user.
     *
     * @param ownerId The integer id of the owner
     *
     * @return Map of ResourceId -> DatabaseMeta
     * <ol>
     *     <li>If the database exists, and the user is the owner of the database, then the DatabaseMeta will be
     *      returned in the map</li>
     *     <li>If the database previously existed but has been deleted, then it will <b>not</b> be returned in the
     *      map</li>
     * </ol>
     */
    Map<ResourceId,DatabaseMeta> getOwnedDatabaseMeta(int ownerId);

    /**
     * Retrieves the DatabaseMeta for the given database.
     *
     * @param databaseId The ResourceId of the database
     *
     * @return Optional DatabaseMeta
     * <ol>
     *     <li>If the database exists, then the DatabaseMeta will be returned wrapped in an Optional</li>
     *     <li>If the database previously existed but has been deleted, then the "deleted" DatabaseMeta will be
     *      returned wrapped in an Optional</li>
     *     <li>If the database does not exist, then an Optional.empty() will be returned</li>
     * </ol>
     */
    Optional<DatabaseMeta> getDatabaseMeta(@NotNull ResourceId databaseId);

    /**
     * Retrieves the DatabaseMeta which contains the given resource.
     *
     * @param resourceId The ResourceId of the resource
     *
     * @return Optional DatabaseMeta
     * <ol>
     *     <li>If the resource's database exists, then the DatabaseMeta will be returned wrapped in an Optional</li>
     *     <li>If the resource's database previously existed but has been deleted, then the "deleted" DatabaseMeta will
     *      be returned wrapped in an Optional</li>
     *     <li>If the database does not exist or cannot be found for the given resource, then an Optional.empty() will
     *      be returned</li>
     * </ol>
     */
    Optional<DatabaseMeta> getDatabaseMetaForResource(@NotNull ResourceId resourceId);

}
