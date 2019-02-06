package org.activityinfo.store.spi;

import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import javax.validation.constraints.NotNull;
import java.util.Map;

public interface DatabaseMetaCache {

    /**
     * Load all requested DatabaseMeta from Session Cache.
     *
     * @param toLoad Map of databaseId of DatabaseMeta -> Memcache Key
     * @return Map of databaseId of DatabaseMeta -> DatabaseMeta (if present)
     */
    @NotNull Map<ResourceId,DatabaseMeta> loadAll(@NotNull Map<ResourceId,String> toLoad);

    /**
     * Put all DatabaseMeta into Session Cache. Overwrites any previous versions stored.
     *
     * @param toStore Map of Memcache Key -> DatabaseMeta
     */
    void putAll(@NotNull Map<String,DatabaseMeta> toStore);

}
