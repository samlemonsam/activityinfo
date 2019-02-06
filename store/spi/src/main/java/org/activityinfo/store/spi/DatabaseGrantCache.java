package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.resource.ResourceId;

import javax.validation.constraints.NotNull;
import java.util.Map;

public interface DatabaseGrantCache {

    /**
     * Session Cache Key for retrieving DatabaseGrants.
     * It differs from the Memcache Key in that it is not dependent on DatabaseGrant or cache version.
     *
     * @param userId the userId of the requesting User
     * @param databaseId the databaseId of the requested DatabaseGrant
     */
    @NotNull String key(int userId, @NotNull ResourceId databaseId);

    /**
     * Load all requested DatabaseGrants from Session Cache.
     *
     * @param toLoad Map of Session Cache Key (UserId:DatabaseId) -> Memcache Key
     * @return Map of Session Cache Key -> DatabaseGrant (if present)
     */
    @NotNull Map<String,DatabaseGrant> loadAll(@NotNull Map<String,String> toLoad);

    /**
     * Put all DatabaseGrants into Session Cache. Overwrites any previous versions stored.
     *
     * @param toStore Map of Memcache Key -> DatabaseGrant
     */
    void putAll(@NotNull Map<String,DatabaseGrant> toStore);

}
